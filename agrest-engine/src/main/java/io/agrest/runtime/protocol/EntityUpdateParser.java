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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @since 5.0
 */
class EntityUpdateParser<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUpdateParser.class);

    private final AgEntity<T> entity;
    private final UpdateRequestParser parent;
    private final JsonValueConverters converters;
    private final BiConsumer<EntityUpdateBuilder<T>, JsonNode> idsExtractor;
    private final Map<String, BiConsumer<EntityUpdateBuilder<T>, JsonNode>> attributeExtractors;
    private final Map<String, BiConsumer<EntityUpdateBuilder<T>, JsonNode>> relationshipExtractors;
    private final Map<String, Predicate<JsonNode>> relationshipIsObjectCheckers;

    public EntityUpdateParser(AgEntity<T> entity, UpdateRequestParser parent, JsonValueConverters converters) {
        this.entity = entity;
        this.parent = parent;
        this.converters = converters;
        this.idsExtractor = createIdsExtractor();
        this.attributeExtractors = new ConcurrentHashMap<>();
        this.relationshipExtractors = new ConcurrentHashMap<>();
        this.relationshipIsObjectCheckers = new ConcurrentHashMap<>();
    }

    public List<EntityUpdate<T>> parse(JsonNode json, int remainingDepth) {
        EntityUpdateBuilder<T> builder = new EntityUpdateBuilder<>(entity, remainingDepth);
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

    private <R> List<EntityUpdate<R>> parseRelated(AgRelationship relationship, JsonNode json, int remainingDepth) {

        if (remainingDepth == 0) {
            LOGGER.info("Truncated updates for relationship '{}' pointing to '{}', as it exceeds the max allowed depth",
                    relationship.getName(),
                    relationship.getTargetEntity().getName());

            return Collections.emptyList();
        }

        AgEntity<R> target = (AgEntity<R>) relationship.getTargetEntity();
        return parent.getParser(target).parse(json, remainingDepth);
    }

    private void processArray(EntityUpdateBuilder<T> builder, JsonNode arrayNode) {
        for (JsonNode node : arrayNode) {
            if (node.isObject()) {
                processObject(builder, node);
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
            return (b, j) -> b.getCurrentUpdate().addIdPart(name, converter.value(j));
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

                b.getCurrentUpdate().addIdPart(n, c.value(idNode));
            });
        }
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createAttributeExtractor(AgAttribute attribute) {
        JsonValueConverter<?> converter = converters.converter(attribute.getType());
        String name = attribute.getName();
        return (b, j) -> b.getCurrentUpdate().setAttribute(name, converter.value(j));
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createRelationshipExtractor(AgRelationship relationship) {
        return relationship.isToMany()
                ? createToManyRelationshipExtractor(relationship)
                : createToOneRelationshipExtractor(relationship);
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createToOneRelationshipExtractor(AgRelationship relationship) {
        JsonValueConverter<?> converter = relatedIdConverter(relationship);
        String name = relationship.getName();
        Predicate<JsonNode> isObject = relationshipIsObjectCheckers
                .computeIfAbsent(name, n -> createRelationshipAsObjectChecker(relationship));

        return (b, j) -> {
            if (isObject.test(j)) {

                List<EntityUpdate<Object>> childUpdates = parseRelated(relationship, j, b.getRemainingDepth() - 1);

                // may be empty if we reached the depth limit
                if (!childUpdates.isEmpty()) {
                    b.getCurrentUpdate().setToOne(name, childUpdates.get(0));
                }

            } else {
                b.getCurrentUpdate().setToOneId(name, converter.value(j));
            }
        };
    }

    private BiConsumer<EntityUpdateBuilder<T>, JsonNode> createToManyRelationshipExtractor(AgRelationship relationship) {

        JsonValueConverter<?> converter = relatedIdConverter(relationship);
        String name = relationship.getName();
        Predicate<JsonNode> isObject = relationshipIsObjectCheckers
                .computeIfAbsent(name, n -> createRelationshipAsObjectChecker(relationship));

        return (b, j) -> {
            if (j.isArray()) {

                if (j.isEmpty()) {
                    b.getCurrentUpdate().emptyToManyIds(name);
                    b.getCurrentUpdate().emptyToMany(name);
                } else {
                    for (JsonNode child : j) {
                        if (isObject.test(child)) {
                            List<EntityUpdate<Object>> childUpdates = parseRelated(relationship, child, b.getRemainingDepth() - 1);

                            // children may be empty if we reached the depth limit
                            if (!childUpdates.isEmpty()) {
                                b.getCurrentUpdate().addToMany(name, childUpdates.get(0));
                            }

                        } else {
                            b.getCurrentUpdate().addToManyId(name, converter.value(child));
                        }
                    }
                }

            } else if (j.isNull()) {
                LOGGER.warn("Unexpected 'null' for a to-many relationship: {}. Skipping...", name);
            } else {
                throw AgException.badRequest("Expected an array for a to-many relationship %s, instead received {}", name, j.asText());
            }
        };
    }

    private Predicate<JsonNode> createRelationshipAsObjectChecker(AgRelationship relationship) {

        // try to guess whether we are dealing with a related ID or a full object

        AgEntity<?> targetEntity = relationship.getTargetEntity();
        return targetEntity.getIdParts().size() == 1
                ? createSingleColumnRelationshipAsObjectChecker()
                : createMultiColumnRelationshipAsObjectChecker(targetEntity);
    }

    private Predicate<JsonNode> createSingleColumnRelationshipAsObjectChecker() {
        // TODO: this will give false positives if the target entity ID single value is a compound object
        return JsonNode::isObject;
    }

    private Predicate<JsonNode> createMultiColumnRelationshipAsObjectChecker(AgEntity<?> targetEntity) {


        Set<String> idNames = targetEntity.getIdParts().stream().map(AgIdPart::getName).collect(Collectors.toSet());
        int idSize = idNames.size();

        return j -> {

            // actually, this is neither an object nor a proper ID
            if (!j.isObject()) {
                return false;
            }

            // JSON will be treated as ID only if it contains all ID values and no other properties

            if (j.size() != idSize) {
                return true;
            }

            for (String idName : idNames) {
                if (j.get(idName) == null) {
                    return true;
                }
            }

            return false;
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
