package io.agrest.jaxrs3.openapi.modelconverter;

import io.agrest.PathConstants;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.core.util.RefUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 5.0
 */
public abstract class AgEntityAwareModelConverter extends AgModelConverter {

    protected Schema doResolveValue(Class<?> type, ModelConverterContext context) {
        Schema primitive = PrimitiveType.createProperty(type);
        return primitive != null
                ? primitive
                : context.resolve(new AnnotatedType().type(type));
    }

    protected Schema doResolveId(AgEntity<?> entity, ModelConverterContext context) {
        switch (entity.getIdParts().size()) {
            case 0:
                return null;
            case 1:
                return doResolveSingleId(entity, context);
            default:
                return doResolveIdMap(entity, context);
        }
    }

    protected Schema doResolveSingleId(AgEntity<?> entity, ModelConverterContext context) {
        AgIdPart idPart = entity.getIdParts().iterator().next();
        return doResolveValue(idPart.getType(), context);
    }

    protected Schema doResolveIdMap(AgEntity<?> entity, ModelConverterContext context) {

        Map<String, Schema> properties = new LinkedHashMap<>();

        List<AgIdPart> sortedIds = new ArrayList<>(entity.getIdParts());
        sortedIds.sort(Comparator.comparing(AgIdPart::getName));
        for (AgIdPart idPart : sortedIds) {
            properties.put(idPart.getName(), doResolveValue(idPart.getType(), context));
        }

        return new ObjectSchema().properties(properties);
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

    protected Map<String, Schema> doResolveProperties(
            Schema idSchema,
            List<Map.Entry<String, Schema>> attributesAndRelationships) {

        // property sorting should be stable and match that of DataResponse: "id" goes first, then a mix of
        // attributes and relationships in alphabetic order
        
        Map<String, Schema> properties = new LinkedHashMap<>();

        if (idSchema != null) {
            properties.put(PathConstants.ID_PK_ATTRIBUTE, idSchema);
        }

        attributesAndRelationships.stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> properties.put(e.getKey(), e.getValue()));

        return properties;
    }
}
