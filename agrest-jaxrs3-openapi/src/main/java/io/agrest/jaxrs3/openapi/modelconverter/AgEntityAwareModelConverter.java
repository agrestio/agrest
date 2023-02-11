package io.agrest.jaxrs3.openapi.modelconverter;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.0
 */
public abstract class AgEntityAwareModelConverter extends AgModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityAwareModelConverter.class);

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
