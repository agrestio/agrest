package io.agrest.jaxrs2.openapi.modelconverter;

import io.agrest.jaxrs2.openapi.TypeWrapper;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * A common superclass of Agrest-provided {@link ModelConverter} objects.
 */
public abstract class AgModelConverter implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgModelConverter.class);

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

    protected Schema doResolveValue(Class<?> type, ModelConverterContext context) {
        Schema primitive = PrimitiveType.createProperty(type);
        return primitive != null
                ? primitive
                : context.resolve(new AnnotatedType().type(type));
    }

    protected Schema doResolveId(AgEntity<?> entity, ModelConverterContext context) {
        if (entity.getIdParts().size() == 1) {
            AgIdPart idPart = entity.getIdParts().iterator().next();
            return doResolveValue(idPart.getType(), context);
        }

        // TODO: resolve multi-IDs as maps
        LOGGER.warn("skipping multi-column ID for entity {}. TODO: need to handle it properly", entity.getName());
        return null;
    }

    protected Schema doResolveRelationship(AgRelationship relationship, ModelConverterContext context) {

        AgEntity<?> targetEntity = relationship.getTargetEntity();

        // ensure related entity and any other entities reachable from it are resolved
        context.resolve(new AnnotatedType().type(targetEntity.getType()));

        // link to resolved entity
        Schema relatedSchemaRef = new Schema().$ref(RefUtils.constructRef(targetEntity.getName()));
        return relationship.isToMany()
                ? new ArraySchema().items(relatedSchemaRef)
                : relatedSchemaRef;
    }

    protected Schema doResolveRelationshipRef(AgRelationship r, ModelConverterContext context) {

        // relationships in updates are resolved as refs to IDs
        Schema idSchema = doResolveId(r.getTargetEntity(), context);
        if (idSchema != null) {
            return r.isToMany() ? new ArraySchema().items(idSchema) : idSchema;
        }

        return null;
    }
}
