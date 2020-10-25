package io.agrest.openapi;

import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.meta.IMetadataService;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
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

    private final IMetadataService metadataService;
    private final List<String> entityPackages;

    public AgEntityModelConverter(
            @Inject IMetadataService metadataService,
            @Inject(BINDING_ENTITY_PACKAGES) List<String> entityPackages) {
        this.metadataService = Objects.requireNonNull(metadataService);
        this.entityPackages = Objects.requireNonNull(entityPackages);
    }

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        if (wrapped != null) {

            Package p = wrapped.getRawClass().getPackage();

            // Since IMetadataService would lazily compile an entity from any Java class,
            // we need to start with a more deterministic filter for the model classes
            return p != null && entityPackages.contains(p.getName());
        }

        return false;
    }

    @Override
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {

        LOGGER.debug("resolve AgEntity ({}}", wrapped);

        AgEntity<?> agEntity = metadataService.getAgEntity(wrapped.getRawClass());
        String name = agEntity.getName();
        Map<String, Schema> properties = new HashMap<>();

        // TODO: should we expose ID attributes for multi-ID entities?
        //  Note that Agrest leaks DB names for IDs that are not mapped as regular attributes
        if (agEntity.getIds().size() == 1) {
            properties.put(PathConstants.ID_PK_ATTRIBUTE, doResolveAttribute(agEntity.getIds().iterator().next(), context));
        }

        for (AgAttribute a : agEntity.getAttributes()) {
            properties.put(a.getName(), doResolveAttribute(a, context));
        }

        for (AgRelationship r : agEntity.getRelationships()) {
            properties.put(r.getName(), doResolveRelationship(r, context));
        }

        Schema<?> schema = new ObjectSchema().name(name).properties(properties);
        return onSchemaResolved(type, context, schema);
    }

    protected Schema doResolveAttribute(AgAttribute attribute, ModelConverterContext context) {
        return context.resolve(new AnnotatedType()
                .name(attribute.getName())
                .type(attribute.getType()));
    }

    protected Schema doResolveRelationship(AgRelationship relationship, ModelConverterContext context) {
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
        return metadataService.equals(that.metadataService) &&
                entityPackages.equals(that.entityPackages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadataService, entityPackages);
    }
}
