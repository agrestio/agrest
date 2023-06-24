package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.PathConstants;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.converter.jsonvalue.MapConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @since 5.0
 */
class EntityUpdateParserStrategy<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUpdateParserStrategy.class);

    private final AgEntity<T> entity;
    private final JsonValueConverters converters;
    private final BiConsumer<EntityUpdateBuilder<T>, JsonNode> idsExtractor;
    private final Map<String, BiConsumer<EntityUpdateBuilder<T>, JsonNode>> attributeExtractors;
    private final Map<String, BiConsumer<EntityUpdateBuilder<T>, JsonNode>> relationshipExtractors;

    public EntityUpdateParserStrategy(AgEntity<T> entity, JsonValueConverters converters) {
        this.entity = entity;
        this.converters = converters;
        this.idsExtractor = createIdsExtractor();
        this.attributeExtractors = new ConcurrentHashMap<>();
        this.relationshipExtractors = new ConcurrentHashMap<>();
    }

    public Collection<EntityUpdate<T>> parse(JsonNode json) {
        EntityUpdateBuilder<T> builder = new EntityUpdateBuilder<>(entity);
        if (json != null) {
            if (json.isArray()) {
                processArray(builder, json);
            } else if (json.isObject()) {
                processObject(builder, json);
            } else {
                throw AgException.badRequest("Expected Object or Array. Got: %s", json.asText());
            }
        }
        // else: empty requests are fine. we just do nothing...

        return builder.getUpdates();
    }

    private void processArray(EntityUpdateBuilder<T> visitor, JsonNode arrayNode) {
        for (JsonNode node : arrayNode) {
            if (node.isObject()) {
                processObject(visitor, node);
            } else {
                throw AgException.badRequest("Expected Object, got: %s", node.asText());
            }
        }
    }

    private void processObject(EntityUpdateBuilder<T> builder, JsonNode objectNode) {

        builder.beginObject();

        Iterator<String> it = objectNode.fieldNames();
        while (it.hasNext()) {
            String key = it.next();

            if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {
                JsonNode valueNode = objectNode.get(key);
                buildIds(builder, valueNode);
                continue;
            }

            AgAttribute attribute = entity.getAttribute(key);
            if (attribute != null) {
                JsonNode valueNode = objectNode.get(key);
                buildAttribute(builder, attribute, valueNode);
                continue;
            }

            AgRelationship relationship = entity.getRelationship(key);
            if (relationship != null) {
                JsonNode valueNode = objectNode.get(key);
                buildRelationship(builder, relationship, valueNode);
                continue;
            }

            LOGGER.info("Skipping unknown property '{}'", key);
        }

        builder.endObject();
    }

    private void buildIds(EntityUpdateBuilder<T> builder, JsonNode json) {
        idsExtractor.accept(builder, json);
    }

    private void buildAttribute(EntityUpdateBuilder<T> builder, AgAttribute attribute, JsonNode json) {
        attributeExtractors.
                computeIfAbsent(attribute.getName(), n -> createAttributeExtractor(attribute))
                .accept(builder, json);
    }

    private void buildRelationship(EntityUpdateBuilder<T> builder, AgRelationship relationship, JsonNode json) {
        relationshipExtractors.
                computeIfAbsent(relationship.getName(), n -> createRelationshipExtractor(relationship))
                .accept(builder, json);
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createIdsExtractor() {
        Collection<AgIdPart> ids = entity.getIdParts();

        if (ids.size() == 1) {
            AgIdPart id = ids.iterator().next();
            JsonValueConverter<?> converter = converters.converter(id.getType());
            String name = id.getName();
            return (b, j) -> b.idPart(name, converter.value(j));
        } else {

            Map<String, JsonValueConverter<?>> idPartsConverters = new HashMap<>(ids.size() * 2);
            for (AgIdPart id : ids) {
                idPartsConverters.put(id.getName(), converters.converter(id.getType()));
            }

            return (b, j) -> idPartsConverters.forEach((n, c) -> {
                JsonNode idNode = j.get(n);
                if (idNode == null) {
                    throw AgException.badRequest("Failed to parse update payload. Id part is missing: " + n);
                }

                b.idPart(n, c.value(idNode));
            });
        }
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createAttributeExtractor(AgAttribute attribute) {
        JsonValueConverter<?> converter = converters.converter(attribute.getType());
        String name = attribute.getName();
        return (b, j) -> b.attribute(name, converter.value(j));
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createRelationshipExtractor(AgRelationship relationship) {
        return relationship.isToMany()
                ? createToManyRelationshipExtractor(relationship)
                : createToOneRelationshipExtractor(relationship);
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createToOneRelationshipExtractor(AgRelationship relationship) {
        JsonValueConverter<?> converter = relatedIdConverter(relationship);
        String name = relationship.getName();
        return (b, j) -> b.relationship(name, converter.value(j));
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createToManyRelationshipExtractor(AgRelationship relationship) {

        JsonValueConverter<?> converter = relatedIdConverter(relationship);
        String name = relationship.getName();

        return (b, j) -> {
            if (j.isArray()) {

                if (j.isEmpty()) {
                    // this is a hackish way to tell the visitor, that it should unrelate all objects
                    b.relationship(name, null);
                } else {
                    for (JsonNode child : j) {
                        b.relationship(name, converter.value(child));
                    }
                }

            } else if (j.isNull()) {
                LOGGER.warn("Unexpected 'null' for a to-many relationship: {}. Skipping...", name);
            } else {
                // TODO: this goes against to-many semantics. Why are we supporting it here?
                b.relationship(name, converter.value(j));
            }
        };
    }

    private JsonValueConverter<?> relatedIdConverter(AgRelationship relationship) {

        Collection<AgIdPart> idParts = relationship.getTargetEntity().getIdParts();

        int len = idParts.size();
        if (len == 1) {
            return converters.converter(idParts.iterator().next().getType());
        }

        Map<String, JsonValueConverter<?>> idPartsConverters = new HashMap<>();
        idParts.forEach(p -> idPartsConverters.put(p.getName(), converters.converter(p.getType())));

        return new MapConverter(idPartsConverters);
    }
}
