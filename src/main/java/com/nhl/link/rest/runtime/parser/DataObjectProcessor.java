package com.nhl.link.rest.runtime.parser;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
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
 * @since 1.11 made public
 */
public class DataObjectProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataObjectProcessor.class);

	protected IJacksonService jsonParser;
	protected IJsonValueConverterFactory converterFactory;
	protected IRelationshipMapper relationshipMapper;

	public DataObjectProcessor(IJacksonService jsonParser, IRelationshipMapper relationshipMapper,
			IJsonValueConverterFactory converterFactory) {
		this.jsonParser = jsonParser;
		this.relationshipMapper = relationshipMapper;
		this.converterFactory = converterFactory;
	}

	public void process(UpdateResponse<?> response, String json) {

		JsonNode node = jsonParser.parseJson(json);
		if (node == null) {
			// empty requests are fine. we just do nothing...
			return;
		} else if (node.isArray()) {
			processArray(response, node);
		} else if (node.isObject()) {
			processObject(response, node);
		} else {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected Object or Array. Got: " + node.asText());
		}
	}

	private void processArray(UpdateResponse<?> response, JsonNode arrayNode) {

		for (JsonNode node : arrayNode) {
			if (node.isObject()) {
				processObject(response, node);
			} else {
				throw new LinkRestException(Status.BAD_REQUEST, "Expected Object, got: " + node.asText());
			}
		}
	}

	private <T> void processObject(UpdateResponse<T> response, JsonNode objectNode) {
		LrEntity<T> entity = response.getEntity().getLrEntity();

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
				Object value = extractValue(valueNode, attribute.getJavaType());
				update.getValues().put(key, value);
				continue;
			}

			LrRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship instanceof LrPersistentRelationship) {

				DbRelationship dbRelationship = ((LrPersistentRelationship) relationship).getObjRelationship()
						.getDbRelationships().get(0);

				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode, dbRelationship);

				// record FK, whether it is a PK or not
				update.getRelatedIds().put(relationship.getName(), value);

				// record FK that is also a PK
				DbRelationship dbRleationship = dbRelationship.getReverseRelationship();
				if (dbRleationship.isToDependentPK()) {
					List<DbJoin> joins = dbRleationship.getJoins();
					if (joins.size() != 1) {
						throw new LinkRestException(Status.BAD_REQUEST,
								"Multi-join relationship propagation is not supported yet: " + entity.getName());
					}

					update.getOrCreateId().put(joins.get(0).getTargetName(), value);
				}

				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}

		// not excluding for empty updates ... we may still need them...
		response.getUpdates().add(update);
	}

	protected void extractPK(EntityUpdate<?> update, JsonNode valueNode) {

		LrPersistentAttribute id = (LrPersistentAttribute) update.getEntity().getSingleId();

		Object value = extractValue(valueNode, id.getJavaType());
		update.getOrCreateId().put(id.getDbAttribute().getName(), value);
	}

	protected Object extractValue(JsonNode valueNode, String javaType) {

		JsonValueConverter converter = converterFactory.converter(javaType);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

	protected Object extractValue(JsonNode valueNode, DbRelationship dbRelationship) {
		int type = dbRelationship.getJoins().get(0).getSource().getType();

		JsonValueConverter converter = converterFactory.converter(type);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST,
					"Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

}
