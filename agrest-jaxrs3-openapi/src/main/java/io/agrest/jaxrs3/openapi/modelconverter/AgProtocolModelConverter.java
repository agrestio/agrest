package io.agrest.jaxrs3.openapi.modelconverter;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs3.openapi.TypeWrapper;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Provides OpenAPI Schema conversions for Agrest protocol objects. The object is stateless singleton.
 */
public class AgProtocolModelConverter extends AgEntityAwareModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgProtocolModelConverter.class);
    private final static String BASE_AG_PACKAGE = AgException.class.getPackage().getName();

    private final AgSchema schema;

    public AgProtocolModelConverter(@Inject AgSchema schema) {
        this.schema = Objects.requireNonNull(schema);
    }

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        if (wrapped != null) {
            Package p = wrapped.getRawClass().getPackage();
            return p != null && p.getName().startsWith(BASE_AG_PACKAGE);
        }

        return false;
    }

    @Override
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {
        if (wrapped.getRawClass() == DataResponse.class) {
            return resolveAsDataResponse(type, context, wrapped);
        } else if (wrapped.getRawClass() == SimpleResponse.class) {
            return resolveAsSimpleResponse(type, context);
        } else if (wrapped.getRawClass() == EntityUpdate.class) {
            return resolveAsEntityUpdate(type, context, wrapped);
        } else {
            // could be an internal test class, etc.
            return delegateResolve(type, context, chain);
        }
    }

    protected boolean isParameterized(TypeWrapper wrapped) {
        // either no parameterization or wildcard
        return wrapped.containedTypeCount() == 1 && wrapped.containedType(0).getRawClass() != Object.class;
    }

    protected Schema resolveAsDataResponse(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        LOGGER.debug("resolve DataResponse ({})", wrapped);

        return isParameterized(wrapped)
                ? resolveAsParameterizedDataResponse(type, context, wrapped)
                : resolveAsRawDataResponse(type, context);
    }

    protected Schema resolveAsParameterizedDataResponse(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        TypeWrapper entityType = wrapped.containedType(0);
        Schema entitySchema = context.resolve(new AnnotatedType().type(entityType.getType()));
        Schema entitySchemaRef = new Schema().$ref(RefUtils.constructRef(entitySchema.getName()));

        Map<String, Schema> properties = new HashMap<>();
        properties.put("data", new ArraySchema().items(entitySchemaRef));
        properties.put("total", new IntegerSchema());

        String name = "DataResponse(" + entitySchema.getName() + ")";
        Schema schema = new ObjectSchema()
                .name(name)
                .required(asList("data", "total"))
                .properties(properties);

        return onSchemaResolved(type, context, schema);
    }

    protected Schema resolveAsRawDataResponse(AnnotatedType type, ModelConverterContext context) {

        Map<String, Schema> properties = new HashMap<>();
        properties.put("data", new ArraySchema());
        properties.put("total", new IntegerSchema());

        String name = "DataResponse(Object)";
        Schema schema = new ObjectSchema()
                .name(name)
                .required(asList("data", "total"))
                .properties(properties);

        return onSchemaResolved(type, context, schema);
    }

    protected Schema resolveAsSimpleResponse(AnnotatedType type, ModelConverterContext context) {

        LOGGER.debug("resolve SimpleResponse");

        String name = "SimpleResponse";
        Map<String, Schema> properties = new HashMap<>();
        properties.put("message", new StringSchema());

        Schema schema = new ObjectSchema()
                .name(name)
                .properties(properties);

        return onSchemaResolved(type, context, schema);
    }

    protected Schema resolveAsEntityUpdate(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        LOGGER.debug("resolve EntityUpdate ({})", wrapped);

        return isParameterized(wrapped)
                ? resolveAsParameterizedEntityUpdate(type, context, wrapped)
                : resolveAsRawEntityUpdate(type, context);
    }

    protected Schema resolveAsRawEntityUpdate(AnnotatedType type, ModelConverterContext context) {
        String name = "EntityUpdate(Object)";
        Schema schema = new ObjectSchema().name(name);
        return onSchemaResolved(type, context, schema);
    }

    protected Schema resolveAsParameterizedEntityUpdate(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        TypeWrapper entityType = wrapped.containedType(0);
        AgEntity<?> entity = schema.getEntity(entityType.getRawClass());
        String name = "EntityUpdate(" + entity.getName() + ")";

        List<Map.Entry<String, Schema>> entries = new ArrayList<>();

        for (AgAttribute a : entity.getAttributes()) {
            entries.add(Map.entry(a.getName(), doResolveValue(a.getType(), context)));
        }

        for (AgRelationship r : entity.getRelationships()) {
            Schema relIdSchema = doResolveRelationshipRef(r, context);
            if (relIdSchema != null) {
                entries.add(Map.entry(r.getName(), relIdSchema));
            }
        }

        Map<String, Schema> properties = doResolveProperties(doResolveId(entity, context), entries);
        Schema<?> schema = new ObjectSchema().name(name).properties(properties);
        return onSchemaResolved(type, context, schema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgProtocolModelConverter that = (AgProtocolModelConverter) o;
        return schema.equals(that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema);
    }
}
