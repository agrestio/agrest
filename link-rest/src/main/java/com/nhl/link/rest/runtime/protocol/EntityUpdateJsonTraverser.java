package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.PathConstants;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
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

    public void traverse(LrEntity<?> entity, JsonNode json, EntityUpdateJsonVisitor visitor) {
        if (json != null) { // empty requests are fine. we just do nothing...
            if (json.isArray()) {
                processArray(entity, json, visitor);
            } else if (json.isObject()) {
                processObject(entity, json, visitor);
            } else {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected Object or Array. Got: " + json.asText());
            }
        }
	}

	private void processArray(LrEntity<?> entity, JsonNode arrayNode, EntityUpdateJsonVisitor visitor) {
		for (JsonNode node : arrayNode) {
			if (node.isObject()) {
				processObject(entity, node, visitor);
			} else {
				throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected Object, got: " + node.asText());
			}
		}
	}

	private void processObject(LrEntity<?> entity, JsonNode objectNode, EntityUpdateJsonVisitor visitor) {

        visitor.beginObject();

		Iterator<String> it = objectNode.fieldNames();
		while (it.hasNext()) {
			String key = it.next();

			if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {
				JsonNode valueNode = objectNode.get(key);
				extractPK(entity, visitor, valueNode);
				continue;
			}

			LrAttribute attribute = entity.getAttribute(key);
			if (attribute != null) {
				JsonNode valueNode = objectNode.get(key);
				Object value = converter(attribute).value(valueNode);
                visitor.visitAttribute(key, value);
				continue;
			}

			LrRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship instanceof LrPersistentRelationship) {
				JsonNode valueNode = objectNode.get(key);
				processRelationship(visitor, (LrPersistentRelationship) relationship, valueNode);
				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}

		visitor.endObject();
	}

	private void processRelationship(EntityUpdateJsonVisitor visitor, LrPersistentRelationship relationship, JsonNode valueNode) {
		if (relationship.isPrimaryKey()) {
			if (valueNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) valueNode;
				if (arrayNode.size() > 1) {
                    throw new LinkRestException(Response.Status.BAD_REQUEST,
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

	private void addRelatedObject(EntityUpdateJsonVisitor visitor, LrRelationship relationship, Object value) {

		// record FK, whether it is a PK or not
		visitor.visitRelationship(relationship.getName(), value);
	}

	protected void extractPK(LrEntity<?> entity, EntityUpdateJsonVisitor visitor, JsonNode valueNode) {

		Collection<LrAttribute> ids = entity.getIds();
		if (ids.size() == 1) {
			extractPKPart(visitor::visitId, ids.iterator().next(), valueNode);
			return;
		}

		for (LrAttribute id : ids) {

			JsonNode idNode = valueNode.get(id.getName());
			if (idNode == null) {
				throw new LinkRestException(Response.Status.BAD_REQUEST,
						"Failed to parse update payload -- ID part is missing: " + id.getName());
			}

			extractPKPart(visitor::visitId, id, idNode);
		}
	}

	protected void extractPKPart(BiConsumer<String, Object> idConsumer, LrAttribute id, JsonNode valueNode) {
		String name;
		if (id instanceof LrPersistentAttribute) {
			name = ((LrPersistentAttribute) id).getColumnName();
		} else {
			name = id.getName();
		}

		Object value = converter(id).value(valueNode);

        idConsumer.accept(name, value);
	}

	private JsonValueConverter<?> converter(LrAttribute attribute) {
		return converterFactory.converter(attribute.getType());
	}

	private JsonValueConverter<?> converter(LrRelationship relationship) {

    	LrEntity<?> target = relationship.getTargetEntity();

		int ids = target.getIds().size();
		if (ids != 1) {
			throw new IllegalArgumentException("Entity '" + target.getName() +
					"' has unexpected number of ID attributes: " + ids);
		}
		return converterFactory.converter(target.getIds().iterator().next().getType());
	}
}
