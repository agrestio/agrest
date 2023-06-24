package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.converter.jsonvalue.MapConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public class EntityUpdateJsonTraverser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUpdateJsonTraverser.class);

    private final IRelationshipMapper relationshipMapper;
    private final JsonValueConverters converters;

    public EntityUpdateJsonTraverser(IRelationshipMapper relationshipMapper, JsonValueConverters converters) {
        this.relationshipMapper = relationshipMapper;
        this.converters = converters;
    }

    public void traverse(AgEntity<?> entity, JsonNode json, EntityUpdateJsonVisitor visitor) {
        if (json != null) { // empty requests are fine. we just do nothing...
            if (json.isArray()) {
                processArray(entity, json, visitor);
            } else if (json.isObject()) {
                processObject(entity, json, visitor);
            } else {
                throw AgException.badRequest("Expected Object or Array. Got: %s", json.asText());
            }
        }
    }

    private void processArray(AgEntity<?> entity, JsonNode arrayNode, EntityUpdateJsonVisitor visitor) {
        for (JsonNode node : arrayNode) {
            if (node.isObject()) {
                processObject(entity, node, visitor);
            } else {
                throw AgException.badRequest("Expected Object, got: %s", node.asText());
            }
        }
    }

    private void processObject(AgEntity<?> entity, JsonNode objectNode, EntityUpdateJsonVisitor visitor) {

        visitor.beginObject();

        Iterator<String> it = objectNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();

            if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {
                JsonNode valueNode = objectNode.get(key);
                extractPK(entity, visitor, valueNode);
                continue;
            }

            AgAttribute attribute = entity.getAttribute(key);
            if (attribute != null) {
                JsonNode valueNode = objectNode.get(key);
                extractAttribute(visitor, attribute, valueNode);
                continue;
            }

            AgRelationship relationship = relationshipMapper.toRelationship(entity, key);
            if (relationship != null) {
                JsonNode valueNode = objectNode.get(key);
                extractRelationship(visitor, relationship, valueNode);
                continue;
            }

            LOGGER.info("Skipping unknown attribute '{}'", key);
        }

        visitor.endObject();
    }

    protected void extractPK(AgEntity<?> entity, EntityUpdateJsonVisitor visitor, JsonNode valueNode) {

        Collection<AgIdPart> ids = entity.getIdParts();
        if (ids.size() == 1) {
            extractPKPart(visitor::visitId, ids.iterator().next(), valueNode);
            return;
        }

        for (AgIdPart id : ids) {

            JsonNode idNode = valueNode.get(id.getName());
            if (idNode == null) {
                throw AgException.badRequest(
                        "Failed to parse update payload -- ID part is missing: " + id.getName());
            }

            extractPKPart(visitor::visitId, id, idNode);
        }
    }

    protected void extractPKPart(BiConsumer<String, Object> idConsumer, AgIdPart id, JsonNode valueNode) {
        idConsumer.accept(
                id.getName(),
                converter(id).value(valueNode));
    }

    private void extractAttribute(EntityUpdateJsonVisitor visitor, AgAttribute attribute, JsonNode valueNode) {
        Object value = converter(attribute).value(valueNode);
        visitor.visitAttribute(attribute.getName(), value);
    }

    private void extractRelationship(EntityUpdateJsonVisitor visitor, AgRelationship relationship, JsonNode valueNode) {

        if (valueNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) valueNode;
            if (arrayNode.size() == 0) {
                // this is kind of a hack/workaround
                visitor.visitRelationship(relationship.getName(), null);
            } else {
                for (int i = 0; i < arrayNode.size(); i++) {
                    visitor.visitRelationship(relationship.getName(), converter(relationship).value(arrayNode.get(i)));
                }
            }
        } else {
            if (relationship.isToMany() && valueNode.isNull()) {
                LOGGER.warn("Unexpected 'null' for a to-many relationship: {}. Skipping...", relationship.getName());
            } else {
                visitor.visitRelationship(relationship.getName(), converter(relationship).value(valueNode));
            }
        }
    }

    private JsonValueConverter<?> converter(AgIdPart idPart) {
        return converters.converter(idPart.getType());
    }

    private JsonValueConverter<?> converter(AgAttribute attribute) {
        return converters.converter(attribute.getType());
    }

    private JsonValueConverter<?> converter(AgRelationship relationship) {

        Collection<AgIdPart> idParts = relationship.getTargetEntity().getIdParts();

        int len = idParts.size();
        if (len == 1) {
            return converters.converter(idParts.iterator().next().getType());
        }

        // TODO: cache compound converter?

        Map<String, JsonValueConverter<?>> idPartsConverters = new HashMap<>();
        idParts.forEach(p -> idPartsConverters.put(p.getName(), converters.converter(p.getType())));

        return new MapConverter(idPartsConverters);
    }
}
