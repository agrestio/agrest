package com.nhl.link.rest.runtime.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

class DataObjectProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataObjectProcessor.class);

	private IJacksonService jsonParser;
	private IJsonValueConverterFactory converterFactory;
	private IRelationshipMapper relationshipMapper;

	DataObjectProcessor(IJacksonService jsonParser, IRelationshipMapper relationshipMapper,
			IJsonValueConverterFactory converterFactory) {
		this.jsonParser = jsonParser;
		this.relationshipMapper = relationshipMapper;
		this.converterFactory = converterFactory;
	}

	void process(UpdateResponse<?> response, String json) {

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

	private void processObject(UpdateResponse<?> response, JsonNode objectNode) {
		ObjEntity entity = response.getEntity().getCayenneEntity();
		Collection<ObjAttribute> pks = entity.getPrimaryKeys();
		ObjAttribute pk = pks.size() == 1 ? pks.iterator().next() : null;

		EntityUpdate update = new EntityUpdate();

		Iterator<String> it = objectNode.fieldNames();
		while (it.hasNext()) {
			String key = it.next();

			if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {

				if (pk == null) {
					throw new IllegalStateException(String.format(
							"Compound ID should't be specified explicitly for entity '%s'", entity.getName()));
				}

				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode, pk);

				update.getOrCreateId().put(pk.getDbAttributeName(), value);
				continue;
			}

			ObjAttribute attribute = (ObjAttribute) entity.getAttribute(key);
			if (attribute != null) {
				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode, attribute);
				update.getValues().put(key, value);
				continue;
			}

			ObjRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship != null) {

				DbRelationship dbRelationship = relationship.getDbRelationships().get(0);

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

	private Object extractValue(JsonNode valueNode, ObjAttribute attribute) {

		JsonValueConverter converter = converterFactory.converter(attribute.getType());

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST, "Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

	private Object extractValue(JsonNode valueNode, DbRelationship dbRelationship) {
		int type = dbRelationship.getJoins().get(0).getSource().getType();

		JsonValueConverter converter = converterFactory.converter(type);

		try {
			return converter.value(valueNode);
		} catch (Exception e) {
			throw new LinkRestException(Status.BAD_REQUEST, "Incorrectly formatted value: '" + valueNode.asText() + "'");
		}
	}

}
