package io.agrest.openapi.modelconverter;

import io.agrest.PathConstants;
import io.agrest.meta.*;
import io.agrest.openapi.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides OpenAPI Schema conversions for Agrest entity objects
 */
public class AgEntityModelConverter extends AgModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityModelConverter.class);

    public static final String BINDING_ENTITY_PACKAGES = "openapi-entity";

    private final AgDataMap dataMap;
    private final List<String> entityPackages;

    public AgEntityModelConverter(
            @Inject AgDataMap dataMap,
            @Inject(BINDING_ENTITY_PACKAGES) List<String> entityPackages) {
        this.dataMap = Objects.requireNonNull(dataMap);
        this.entityPackages = Objects.requireNonNull(entityPackages);
    }

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        if (wrapped != null) {

            Package p = wrapped.getRawClass().getPackage();

            // Since AgDataMap would lazily compile an entity from any Java class,
            // we need to start with a more deterministic filter for the model classes
            return p != null && entityPackages.contains(p.getName());
        }

        return false;
    }

    @Override
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {

        LOGGER.debug("resolve AgEntity ({}}", wrapped);

        AgEntity<?> agEntity = dataMap.getEntity(wrapped.getRawClass());
        String name = agEntity.getName();
        Map<String, Schema> properties = new HashMap<>();

        // TODO: multi-key ids must be exposed as maps
        if (agEntity.getIdParts().size() == 1) {
            AgIdPart id = agEntity.getIdParts().iterator().next();
            properties.put(PathConstants.ID_PK_ATTRIBUTE, doResolveValue(
                    PathConstants.ID_PK_ATTRIBUTE,
                    id.getType(),
                    context));
        }

        for (AgAttribute a : agEntity.getAttributes()) {
            properties.put(a.getName(), doResolveValue(a.getName(), a.getType(), context));
        }

        for (AgRelationship r : agEntity.getRelationships()) {
            properties.put(r.getName(), doResolveRelationship(r));
        }

        Schema<?> schema = new ObjectSchema().name(name).properties(properties);
        return onSchemaResolved(type, context, schema);
    }

    protected Schema doResolveValue(String name, Class<?> type, ModelConverterContext context) {
        Schema primitive = PrimitiveType.createProperty(type);
        return primitive != null
                ? primitive
                : context.resolve(new AnnotatedType().type(type));
    }

    protected Schema doResolveRelationship(AgRelationship relationship) {
        AgEntity<?> targetEntity = relationship.getTargetEntity();
        Schema relatedSchemaRef = new Schema().$ref(RefUtils.constructRef(targetEntity.getName()));
        return relationship.isToMany()
                ? new ArraySchema().items(relatedSchemaRef)
                : relatedSchemaRef;
    }

    // implementing equals/hashCode to be able to detect previously installed converters in the static context

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgEntityModelConverter that = (AgEntityModelConverter) o;
        return dataMap.equals(that.dataMap) &&
                entityPackages.equals(that.entityPackages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataMap, entityPackages);
    }
}
