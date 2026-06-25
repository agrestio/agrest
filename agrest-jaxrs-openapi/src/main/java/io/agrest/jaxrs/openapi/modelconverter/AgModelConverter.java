package io.agrest.jaxrs.openapi.modelconverter;

import io.agrest.jaxrs.openapi.TypeWrapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

/**
 * A common superclass of Agrest-provided {@link ModelConverter} objects.
 */
public abstract class AgModelConverter implements ModelConverter {

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {

        Schema existing = context.resolve(type);
        if (existing != null) {
            return existing;
        }

        TypeWrapper wrapped = TypeWrapper.forType(type.getType());
        return willResolve(type, context, wrapped)
                ? doResolve(type, context, chain, wrapped)
                : delegateResolve(type, context, chain);
    }

    protected abstract boolean willResolve(AnnotatedType type, ModelConverterContext context, TypeWrapper wrapped);

    protected abstract Schema doResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain, TypeWrapper wrapped);

    protected Schema delegateResolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }

    protected Schema onSchemaResolved(AnnotatedType type, ModelConverterContext context, Schema resolved) {
        context.defineModel(resolved.getName(), resolved);
        return type.isResolveAsRef()
                ? new Schema().$ref(RefUtils.constructRef(resolved.getName()))
                : resolved;
    }
}
