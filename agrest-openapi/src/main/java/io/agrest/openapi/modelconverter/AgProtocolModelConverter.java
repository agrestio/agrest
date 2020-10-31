package io.agrest.openapi.modelconverter;

import io.agrest.*;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.openapi.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.*;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Provides OpenAPI Schema conversions for Agrest protocol objects. The object is stateless singelton
 */
public class AgProtocolModelConverter extends AgModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgProtocolModelConverter.class);
    private final static String BASE_AG_PACKAGE = Ag.class.getPackage().getName();

    private final AgDataMap dataMap;

    public AgProtocolModelConverter(@Inject AgDataMap dataMap) {
        this.dataMap = Objects.requireNonNull(dataMap);
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
        properties.put("success", new BooleanSchema());
        properties.put("message", new StringSchema());

        Schema schema = new ObjectSchema()
                .name(name)
                .required(asList("success"))
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
        AgEntity<?> agEntity = dataMap.getEntity(entityType.getRawClass());
        String name = "EntityUpdate(" + agEntity.getName() + ")";
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

        // TODO: include FKs of to-one and to many ids collections

        Schema<?> schema = new ObjectSchema().name(name).properties(properties);
        return onSchemaResolved(type, context, schema);
    }

    // TODO: duplicate of a method in AgEntityModelConverter
    protected Schema doResolveValue(String name, Class<?> type, ModelConverterContext context) {
        Schema primitive = PrimitiveType.createProperty(type);
        return primitive != null
                ? primitive
                : context.resolve(new AnnotatedType().type(type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgProtocolModelConverter that = (AgProtocolModelConverter) o;
        return dataMap.equals(that.dataMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataMap);
    }
}
