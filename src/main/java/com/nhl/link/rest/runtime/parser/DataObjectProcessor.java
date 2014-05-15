package com.nhl.link.rest.runtime.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.parser.converter.UtcDateConverter;
import com.nhl.link.rest.runtime.parser.converter.ValueConverter;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

class DataObjectProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataObjectProcessor.class);

	private RequestJsonParser jsonParser;
	private Map<String, ValueConverter> converters;
	private IRelationshipMapper relationshipMapper;

	DataObjectProcessor(RequestJsonParser jsonParser, IRelationshipMapper relationshipMapper) {
		this.jsonParser = jsonParser;
		this.relationshipMapper = relationshipMapper;

		// TODO: unify converters between CayenneExpProcessor and
		// DataObjectProcessor
		this.converters = new HashMap<>();
		this.converters.put(Date.class.getName(), new UtcDateConverter());
	}

	void process(UpdateResponse<?> response, String json) {

		JsonNode objectNode = jsonParser.parseJSON(json, new ObjectMapper());
		if(objectNode == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No JSON in the body");
		}

		ObjEntity entity = response.getEntity().getEntity();

		Iterator<String> it = objectNode.fieldNames();
		while (it.hasNext()) {
			String key = it.next();

			// Ignore ID key. It is there, but we don't need it.
			// On INSERT it may contain some dummy value, like "0"
			if (PathConstants.ID_PK_ATTRIBUTE.equals(key)) {
				continue;
			}

			ObjAttribute attribute = (ObjAttribute) entity.getAttribute(key);
			if (attribute != null) {
				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode, attribute);
				response.getValues().put(key, value);
				continue;
			}

			ObjRelationship relationship = relationshipMapper.toRelationship(entity, key);
			if (relationship != null) {
				JsonNode valueNode = objectNode.get(key);
				Object value = extractValue(valueNode);
				response.getRelatedIds().put(relationship.getName(), value);
				continue;
			}

			LOGGER.info("Skipping unknown attribute '" + key + "'");
		}
	}

	private Object extractValue(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

		switch (type) {
		case VALUE_NUMBER_INT:
			return valueNode.asInt();
		case VALUE_NUMBER_FLOAT:
			return valueNode.asDouble();
		case VALUE_TRUE:
			return Boolean.TRUE;
		case VALUE_FALSE:
			return Boolean.FALSE;
		case VALUE_NULL:
			return null;
		default:
			return valueNode.asText();
		}
	}

	private Object extractValue(JsonNode valueNode, ObjAttribute attribute) {
		JsonToken type = valueNode.asToken();

		switch (type) {
		case VALUE_NUMBER_INT:
			return valueNode.asInt();
		case VALUE_NUMBER_FLOAT:
			return valueNode.asDouble();
		case VALUE_TRUE:
			return Boolean.TRUE;
		case VALUE_FALSE:
			return Boolean.FALSE;
		case VALUE_NULL:
			return null;
		default:
			String text = valueNode.asText();
			ValueConverter converter = converters.get(attribute.getType());
			if (converter == null) {
				return text;
			}

			try {
				return converter.value(text);
			} catch (Exception e) {
				throw new LinkRestException(Status.BAD_REQUEST, "Incorrectly formatted value: '" + text + "'");
			}
		}
	}

}
