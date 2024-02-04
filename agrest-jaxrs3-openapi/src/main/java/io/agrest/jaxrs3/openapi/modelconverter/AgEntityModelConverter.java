package io.agrest.jaxrs3.openapi.modelconverter;

import io.agrest.jaxrs3.openapi.TypeWrapper;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides OpenAPI Schema conversions for Agrest entity objects
 */
public class AgEntityModelConverter extends AgEntityAwareModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityModelConverter.class);

    public static final String BINDING_ENTITY_PACKAGES = "openapi-entity";

    private final AgSchema schema;
    private final List<String> entityPackages;

    public AgEntityModelConverter(
            @Inject AgSchema schema,
            @Inject(BINDING_ENTITY_PACKAGES) List<String> entityPackages) {
        this.schema = Objects.requireNonNull(schema);
        this.entityPackages = Objects.requireNonNull(entityPackages);
    }

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        if (wrapped != null) {

            Package p = wrapped.getRawClass().getPackage();

            // Since AgSchema would lazily compile an entity from any Java class,
            // we need to start with a more deterministic filter for the model classes
            return p != null && entityPackages.contains(p.getName());
        }

        return false;
    }

    @Override
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {

        LOGGER.debug("resolve AgEntity ({}}", wrapped);

        AgEntity<?> entity = schema.getEntity(wrapped.getRawClass());
        String name = entity.getName();

        List<Map.Entry<String, Schema>> entries = new ArrayList<>();
        PropertyAccessChecker accessChecker = PropertyAccessChecker.checkRead();

        for (AgAttribute a : entity.getAttributes()) {
            Schema aSchema = doResolveAttribute(a, context, accessChecker);
            if (aSchema != null) {
                entries.add(Map.entry(a.getName(), aSchema));
            }
        }

        for (AgRelationship r : entity.getRelationships()) {
            Schema relSchema = doResolveRelationship(r, context, accessChecker);
            if (relSchema != null) {
                entries.add(Map.entry(r.getName(), relSchema));
            }
        }

        Map<String, Schema> properties = doCollectProperties(doResolveId(entity, context, accessChecker), entries);
        Schema<?> schema = new ObjectSchema().name(name).properties(properties);
        return onSchemaResolved(type, context, schema);
    }

    // implementing equals/hashCode to be able to detect previously installed converters in the static context

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgEntityModelConverter that = (AgEntityModelConverter) o;
        return schema.equals(that.schema) &&
                entityPackages.equals(that.entityPackages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, entityPackages);
    }
}
