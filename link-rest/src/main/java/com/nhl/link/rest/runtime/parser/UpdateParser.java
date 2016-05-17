package com.nhl.link.rest.runtime.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * @since 1.20
 */
public class UpdateParser implements IUpdateParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateParser.class);

	protected IJsonValueConverterFactory converterFactory;
	protected IRelationshipMapper relationshipMapper;
	protected IJacksonService jacksonService;

	public UpdateParser(@Inject IRelationshipMapper relationshipMapper,
			@Inject IJsonValueConverterFactory converterFactory, @Inject IJacksonService jacksonService) {
		this.relationshipMapper = relationshipMapper;
		this.converterFactory = converterFactory;
		this.jacksonService = jacksonService;
	}

	@Override
	public <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, InputStream entityStream) {
		JsonNode node = jacksonService.parseJson(entityStream);
		return parse(entity, node);
	}

	@Override
	public <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, String entityData) {
		JsonNode node = jacksonService.parseJson(entityData);
		return parse(entity, node);
	}

	protected <T> Collection<EntityUpdate<T>> parse(LrEntity<T> entity, JsonNode json) {

		if (json == null) {
			// empty requests are fine. we just do nothing...
			return Collections.emptyList();
		} else if (json.isArray()) {
			return processArray(entity, json);
		} else if (json.isObject()) {
			return Collections.singletonList(processObject(entity, json));
		} else {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected Object or Array. Got: " + json.asText());
		}
	}

	private <T> Collection<EntityUpdate<T>> processArray(LrEntity<T> entity, JsonNode arrayNode) {

		Collection<EntityUpdate<T>> updates = new ArrayList<>();
		for (JsonNode node : arrayNode) {
			if (node.isObject()) {
				updates.add(processObject(entity, node));
			} else {
				throw new LinkRestException(Status.BAD_REQUEST, "Expected Object, got: " + node.asText());
			}
		}

		return updates;
	}

	private <T> EntityUpdate<T> processObject(LrEntity<T> entity, JsonNode objectNode) {

		EntityUpdate<T> update = new EntityUpdate<>(entity);

		Iterator<String> it = objectNode.fieldNames();
		while (it.hasNext()) {
			String key = it.next();

			if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {
				JsonNode valueNode = objectNode.get(key);
				extractPK(update, valueNode);
				continue;
			}

			LrAttribute attribute = entity.getAttribute(key);
			if (attribute != null) {
				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode, attribute.getType());
				update.getValues().put(key, value);
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
						throw new LinkRestException(Status.BAD_REQUEST,
								"Multi-join relationship propagation is not supported yet: " + entity.getName());
					}
					DbJoin reversePkJoin = joins.get(0);

					Object value = null;
					if (valueNode.isArray()) {
						ArrayNode arrayNode = (ArrayNode) valueNode;
						if (arrayNode.size() > 1) {
							throw new LinkRestException(Status.BAD_REQUEST,
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
						update.getOrCreateId().put(reversePkJoin.getTargetName(), value);
					}

				}

				if (valueNode.isArray()) {
					ArrayNode arrayNode = (ArrayNode) valueNode;

					int len = arrayNode.size();

					if (len == 0) {
						// this is kind of a a hack/workaround
						addRelatedObject(update, relationship, null);
					} else {
						for (int i = 0; i < len; i++) {
							valueNode = arrayNode.get(i);
							addRelatedObject(update, relationship, extractValue(valueNode, targetJdbcType));
						}
					}
				} else {
					addRelatedObject(update, relationship, extractValue(valueNode, targetJdbcType));
				}

				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}

		// not excluding empty updates ... we may still need them...
		return update;
	}

	private void addRelatedObject(EntityUpdate<?> update, LrRelationship relationship, Object value) {

		// record FK, whether it is a PK or not
		update.addRelatedId(relationship.getName(), value);
	}

	protected void extractPK(EntityUpdate<?> update, JsonNode valueNode) {

		Collection<LrAttribute> ids = update.getEntity().getIds();
		for (LrAttribute id : ids) {
			boolean persistent = id instanceof LrPersistentAttribute;
			JsonNode idNode;
			if (ids.size() > 1) {
				idNode = valueNode.get(id.getName());
				if (idNode == null) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Failed to parse update payload -- ID part is missing: " + id.getName());
				}
			} else {
				idNode = valueNode;
			}
			Object value = extractValue(idNode, persistent ? ((LrPersistentAttribute) id).getJdbcType() : Integer.MIN_VALUE);
			update.getOrCreateId().put(persistent? ((LrPersistentAttribute) id).getDbAttribute().getName() : id.getName(), value);
		}
	}

	protected Object extractValue(JsonNode valueNode, Class<?> javaType) {

		JsonValueConverter converter = converterFactory.converter(javaType);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

	protected Object extractValue(JsonNode valueNode, int type) {

		JsonValueConverter converter = converterFactory.converter(type);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

}
