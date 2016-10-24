package com.nhl.link.rest.runtime.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public class EntityJsonTraverser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityJsonTraverser.class);

    private IRelationshipMapper relationshipMapper;
    private IJsonValueConverterFactory converterFactory;

    public EntityJsonTraverser(IRelationshipMapper relationshipMapper, IJsonValueConverterFactory converterFactory) {
        this.relationshipMapper = relationshipMapper;
        this.converterFactory = converterFactory;
    }

    public void traverse(LrEntity<?> entity, JsonNode json, EntityJsonVisitor visitor) {
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

	private void processArray(LrEntity<?> entity, JsonNode arrayNode, EntityJsonVisitor visitor) {
		for (JsonNode node : arrayNode) {
			if (node.isObject()) {
				processObject(entity, node, visitor);
			} else {
				throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected Object, got: " + node.asText());
			}
		}
	}

	private void processObject(LrEntity<?> entity, JsonNode objectNode, EntityJsonVisitor visitor) {

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
				Object value = extractValue(valueNode, attribute.getType());
                visitor.visitAttribute(key, value);
				continue;
			}

			LrRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship instanceof LrPersistentRelationship) {

				List<DbRelationship> dbRelationshipsList = ((LrPersistentRelationship) relationship)
						.getObjRelationship().getDbRelationships();

				// take last element from list of db relationships
				// in order to behave correctly if
				// db entities are connected through intermediate tables
				DbRelationship targetRelationship = dbRelationshipsList.get(dbRelationshipsList.size() - 1);
				int targetJdbcType = targetRelationship.getJoins().get(0).getTarget().getType();

				JsonNode valueNode = objectNode.get(key);
				DbRelationship reverseRelationship = dbRelationshipsList.get(0).getReverseRelationship();
				if (reverseRelationship.isToDependentPK()) {
					List<DbJoin> joins = reverseRelationship.getJoins();
					if (joins.size() != 1) {
						throw new LinkRestException(Response.Status.BAD_REQUEST,
								"Multi-join relationship propagation is not supported yet: " + entity.getName());
					}
					DbJoin reversePkJoin = joins.get(0);

					Object value = null;
					if (valueNode.isArray()) {
						ArrayNode arrayNode = (ArrayNode) valueNode;
						if (arrayNode.size() > 1) {
							throw new LinkRestException(Response.Status.BAD_REQUEST,
								"Relationship is a part of the primary key, only one related object allowed: "
										+ reversePkJoin.getTargetName());
						} else if (arrayNode.size() == 1) {
							value = extractValue(arrayNode.get(0), targetJdbcType);
						}
					} else {
						value = extractValue(valueNode, targetJdbcType);
					}

					if (value != null) {
						// record FK that is also a PK
                        visitor.visitId(reversePkJoin.getTargetName(), value);
					}

				}

				if (valueNode.isArray()) {
					ArrayNode arrayNode = (ArrayNode) valueNode;

					int len = arrayNode.size();

					if (len == 0) {
						// this is kind of a a hack/workaround
						addRelatedObject(visitor, relationship, null);
					} else {
						for (int i = 0; i < len; i++) {
							valueNode = arrayNode.get(i);
							addRelatedObject(visitor, relationship, extractValue(valueNode, targetJdbcType));
						}
					}
				} else {
					if (relationship.isToMany() && valueNode.isNull()) {
						LOGGER.warn("Unexpected 'null' for a to-many relationship: " + relationship.getName()
								+ ". Skipping...");
					} else {
						addRelatedObject(visitor, relationship, extractValue(valueNode, targetJdbcType));
					}
				}

				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}

		visitor.endObject();
	}

	private void addRelatedObject(EntityJsonVisitor visitor, LrRelationship relationship, Object value) {

		// record FK, whether it is a PK or not
		visitor.visitRelationship(relationship.getName(), value);
	}

	protected void extractPK(LrEntity<?> entity, EntityJsonVisitor visitor, JsonNode valueNode) {

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

		int type = Integer.MIN_VALUE;
		String name = id.getName();

		if (id instanceof LrPersistentAttribute) {
			LrPersistentAttribute persistentId = (LrPersistentAttribute) id;
			type = persistentId.getJdbcType();
			name = persistentId.getDbAttribute().getName();
		}

		Object value = extractValue(valueNode, type);

        idConsumer.accept(name, value);
	}

	protected Object extractValue(JsonNode valueNode, Class<?> javaType) {

		JsonValueConverter converter = converterFactory.converter(javaType);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Response.Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'", e);
		}
	}

	protected Object extractValue(JsonNode valueNode, int type) {

		JsonValueConverter converter = converterFactory.converter(type);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Response.Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'", e);
		}
	}
}
