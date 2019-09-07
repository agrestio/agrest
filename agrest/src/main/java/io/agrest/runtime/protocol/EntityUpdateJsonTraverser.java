package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentRelationship;
import io.agrest.meta.AgRelationship;
import io.agrest.parser.converter.JsonValueConverter;
import io.agrest.runtime.parser.converter.IJsonValueConverterFactory;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class EntityUpdateJsonTraverser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUpdateJsonTraverser.class);

    private IRelationshipMapper relationshipMapper;
	private IJsonValueConverterFactory converterFactory;

    public EntityUpdateJsonTraverser(IRelationshipMapper relationshipMapper, IJsonValueConverterFactory converterFactory) {
        this.relationshipMapper = relationshipMapper;
		this.converterFactory = converterFactory;
    }

    public void traverse(AgEntity<?> entity, JsonNode json, EntityUpdateJsonVisitor visitor) {
        if (json != null) { // empty requests are fine. we just do nothing...
            if (json.isArray()) {
                processArray(entity, json, visitor);
            } else if (json.isObject()) {
                processObject(entity, json, visitor);
            } else {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected Object or Array. Got: " + json.asText());
            }
        }
	}

	private void processArray(AgEntity<?> entity, JsonNode arrayNode, EntityUpdateJsonVisitor visitor) {
		for (JsonNode node : arrayNode) {
			if (node.isObject()) {
				processObject(entity, node, visitor);
			} else {
				throw new AgException(Response.Status.BAD_REQUEST, "Expected Object, got: " + node.asText());
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
				Object value = converter(attribute).value(valueNode);
                visitor.visitAttribute(key, value);
				continue;
			}

			AgRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship instanceof AgPersistentRelationship) {
				JsonNode valueNode = objectNode.get(key);
				processRelationship(visitor, (AgPersistentRelationship) relationship, valueNode);
				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}

		visitor.endObject();
	}

	private void processRelationship(EntityUpdateJsonVisitor visitor, AgPersistentRelationship relationship, JsonNode valueNode) {
		if (relationship.isPrimaryKey()) {
			if (valueNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) valueNode;
				if (arrayNode.size() > 1) {
                    throw new AgException(Response.Status.BAD_REQUEST,
                        "Relationship is a part of the primary key, only one related object allowed: "
                                + relationship.getName());
                } else if (arrayNode.size() == 1) {
					valueNode = arrayNode.get(0);
				}
			}
			// record FK that is also a PK
			relationship.extractId(valueNode).forEach(visitor::visitId);
		}

		if (valueNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) valueNode;
            if (arrayNode.size() == 0) {
				// this is kind of a a hack/workaround
				addRelatedObject(visitor, relationship, null);
			} else {
				for (int i = 0; i < arrayNode.size(); i++) {
					addRelatedObject(visitor, relationship, converter(relationship).value(arrayNode.get(i)));
				}
			}
        } else {
            if (relationship.isToMany() && valueNode.isNull()) {
                LOGGER.warn("Unexpected 'null' for a to-many relationship: " + relationship.getName()
                        + ". Skipping...");
            } else {
                addRelatedObject(visitor, relationship, converter(relationship).value(valueNode));
            }
        }
	}

	private void addRelatedObject(EntityUpdateJsonVisitor visitor, AgRelationship relationship, Object value) {

		// record FK, whether it is a PK or not
		visitor.visitRelationship(relationship.getName(), value);
	}

	protected void extractPK(AgEntity<?> entity, EntityUpdateJsonVisitor visitor, JsonNode valueNode) {

		Collection<AgAttribute> ids = entity.getIds();
		if (ids.size() == 1) {
			extractPKPart(visitor::visitId, ids.iterator().next(), valueNode);
			return;
		}

		for (AgAttribute id : ids) {

			JsonNode idNode = valueNode.get(id.getName());
			if (idNode == null) {
				throw new AgException(Response.Status.BAD_REQUEST,
						"Failed to parse update payload -- ID part is missing: " + id.getName());
			}

			extractPKPart(visitor::visitId, id, idNode);
		}
	}

	protected void extractPKPart(BiConsumer<String, Object> idConsumer, AgAttribute id, JsonNode valueNode) {
        idConsumer.accept(
        		id.getName(),
				converter(id).value(valueNode));
	}

	private JsonValueConverter<?> converter(AgAttribute attribute) {
		return converterFactory.converter(attribute.getType());
	}

	private JsonValueConverter<?> converter(AgRelationship relationship) {

    	AgEntity<?> target = relationship.getTargetEntity();

		int ids = target.getIds().size();
		if (ids != 1) {
			throw new IllegalArgumentException("Entity '" + target.getName() +
					"' has unexpected number of ID attributes: " + ids);
		}
		return converterFactory.converter(target.getIds().iterator().next().getType());
	}
}
