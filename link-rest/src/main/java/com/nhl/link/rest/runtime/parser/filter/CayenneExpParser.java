package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.protocol.CayenneExp;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CayenneExpParser implements ICayenneExpParser {

	private static final String EXP = "exp";
	private static final String PARAMS = "params";

	private IJacksonService jsonParser;

	public CayenneExpParser(@Inject IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	/**
	 * @since 2.13
	 */
	@Override
	public CayenneExp fromString(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		CayenneExp cayenneExp;

		if (value.startsWith("[")) {
			cayenneExp = fromArray(jsonParser.parseJson(value));
		} else if (value.startsWith("{")) {
			cayenneExp = fromJson(jsonParser.parseJson(value));
		} else {
			cayenneExp = new CayenneExp(value);
		}

		return cayenneExp;
	}

	/**
	 * @since 2.13
	 */
	@Override
	public CayenneExp fromRootNode(JsonNode root) {
		JsonNode expNode = root.get(CayenneExp.CAYENNE_EXP);

		if (expNode != null) {
			return fromString(expNode.isTextual() ? expNode.asText() : expNode.toString());
		}

		return null;
	}

	private CayenneExp fromJson(JsonNode node) {
		// 'exp' key is required; 'params' key is optional
		JsonNode expNode = node.get(EXP);
		if (expNode == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "'exp' key is missing in 'cayenneExp' map");
		}

		JsonNode paramsNode = node.get(PARAMS);
		if (paramsNode != null) {

			Map<String, Object> paramsMap = new HashMap<>();

			Iterator<String> it = paramsNode.fieldNames();
			while (it.hasNext()) {
				String key = it.next();
				JsonNode valueNode = paramsNode.get(key);
				Object val = extractValue(valueNode);
				paramsMap.put(key, val);
			}

			return new CayenneExp(expNode.asText(), paramsMap);
		}

		return new CayenneExp(expNode.asText());
	}

	public CayenneExp fromArray(JsonNode array) {
		int len = array.size();

		if (len < 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "array 'cayenneExp' mast have at least one element");
		}

		String expString = array.get(0).asText();

		if (len < 2) {
			return new CayenneExp(expString);
		}

		Object[] params = new Object[len - 1];

		for (int i = 1; i < len; i++) {

			JsonNode paramNode = array.get(i);
			params[i - 1] = extractValue(paramNode);
		}

		return new CayenneExp(expString, params);
	}

	private static Object extractValue(JsonNode valueNode) {
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
		case START_ARRAY:
			return extractArray(valueNode);
		default:
			// String parameters may need to be parsed further. Defer parsing
			// until it is placed in the context of an expression...
			return valueNode;
		}
	}

	private static List<Object> extractArray(JsonNode arrayNode) {
		List<Object> values = new ArrayList<>(arrayNode.size());

		for (JsonNode value : arrayNode) {
			values.add(extractValue(value));
		}

		return values;
	}
}
