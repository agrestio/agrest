package io.agrest.openapi.modelconverter;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.openapi.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Provides OpenAPI Schema conversions for Agrest protocol objects. The object is stateless singelton
 */
public class AgProtocolModelConverter extends AgModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgProtocolModelConverter.class);
    private final static String BASE_AG_PACKAGE = Ag.class.getPackage().getName();
    private static final AgProtocolModelConverter instance = new AgProtocolModelConverter();

    public static AgProtocolModelConverter getInstance() {
        return instance;
    }

    protected AgProtocolModelConverter() {
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
        } else {
            // could be an internal test class, etc.
            return delegateResolve(type, context, chain);
        }
    }

    protected Schema resolveAsDataResponse(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        LOGGER.debug("resolve DataResponse ({})", wrapped);


        return wrapped.containedTypeCount() == 1
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
}
