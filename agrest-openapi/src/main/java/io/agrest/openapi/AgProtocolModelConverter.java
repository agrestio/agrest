package io.agrest.openapi;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        if (wrapped.getRawClass() == DataResponse.class) {
            return resolveAsDataResponse(type, context, wrapped);
        } else if (wrapped.getRawClass() == SimpleResponse.class) {
            return resolveAsSimpleResponse(type, context);
        } else {
            // TODO: throw an exception here once we properly support all Agrest types
            LOGGER.warn("This Agrest type should not be exposed as an OpenAPI model: {}", wrapped);
            return null;
        }
    }

    protected Schema resolveAsDataResponse(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {

        LOGGER.debug("resolve DataResponse ({})", wrapped);


        Map<String, Schema> properties = new HashMap<>();

        // TODO: handled non-generified DataResponse too
        TypeWrapper entityType = wrapped.containedType(0);
        Schema entitySchema = context.resolve(new AnnotatedType().type(entityType.getType()));
        Schema entitySchemaRef = new Schema().$ref(RefUtils.constructRef(entitySchema.getName()));

        properties.put("data", new ArraySchema().items(entitySchemaRef));
        properties.put("total", new IntegerSchema());

        String name = "DataResponse(" + entitySchema.getName() + ")";
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
