package io.agrest.jaxrs.openapi.modelconverter;

import io.agrest.jaxrs.openapi.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AgValueModelConverter extends AgModelConverter {

    private static final AgValueModelConverter instance = new AgValueModelConverter();

    public static AgValueModelConverter getInstance() {
        return instance;
    }

    private final Map<Class, Supplier<Schema>> resolvers;

    protected AgValueModelConverter() {
        this.resolvers = new ConcurrentHashMap<>();
        this.resolvers.put(byte[].class, this::stringByteSchema);
        this.resolvers.put(LocalTime.class, this::stringNullSchema);
    }

    protected Schema stringByteSchema() {
        // Schemas are mutable, so creating a new one every time
        return new BinarySchema().format("byte");
    }

    protected Schema stringNullSchema() {
        // Schemas are mutable, so creating a new one every time
        return new StringSchema();
    }

    @Override
    protected boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped) {
        return wrapped != null && resolvers.containsKey(wrapped.getRawClass());
    }

    @Override
    protected Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped) {
        Supplier<Schema> resolver = resolvers.get(wrapped.getRawClass());
        Schema schema = resolver != null ? resolver.get() : null;
        return schema != null ? schema : delegateResolve(type, context, chain);
    }
}
