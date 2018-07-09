package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import org.apache.cayenne.exp.Expression;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ParamConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class CayenneExpConverter implements ParamConverter<CayenneExp> {

	private static final String EXP = "exp";
	private static final String PARAMS = "params";

	private IJacksonService jsonParser;

	public CayenneExpConverter(IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	Expression exp(String value) {

		return exp(fromString(value));
	}

	Expression exp(JsonNode value) {
		if (value == null) {
			return null;
		}

		CayenneExp cayenneExp;

		if (value.isArray()) {
			cayenneExp = processArray(value);
		} else if (value.isObject()) {
			cayenneExp = processMap(value);
		} else {
			cayenneExp = processExp(value.asText());
		}

		return cayenneExp.toExpression();
	}

	/**
	 * @since 2.13
	 */
	Expression exp(CayenneExp cayenneExp) {
		if (cayenneExp == null) {
			return null;
		}

		return cayenneExp.toExpression();
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
			cayenneExp = processArray(jsonParser.parseJson(value));
		} else if (value.startsWith("{")) {
			cayenneExp = processMap(jsonParser.parseJson(value));
		} else {
			cayenneExp = processExp(value);
		}

		return cayenneExp;
	}

	/**
	 * @since 2.13
	 */
	@Override
	public String toString(CayenneExp value) {
		return null;
	}

	private CayenneExp processExp(String value) {
		CayenneExp cayenneExp = new CayenneExp();
		cayenneExp.setExp(value);
		return cayenneExp;
	}

	private CayenneExp processMap(JsonNode map) {
		// 'exp' key is required; 'params' key is optional
		JsonNode expNode = map.get(EXP);
		if (expNode == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "'exp' key is missing in 'cayenneExp' map");
		}

		CayenneExp cayenneExp = new CayenneExp();
		cayenneExp.setExp(expNode.asText());

		JsonNode paramsNode = map.get(PARAMS);
		if (paramsNode != null) {

			Map<String, Object> paramsMap = new HashMap<>();

			Iterator<String> it = paramsNode.fieldNames();
			while (it.hasNext()) {
				String key = it.next();
				JsonNode valueNode = paramsNode.get(key);
				Object val = extractValue(valueNode);
				paramsMap.put(key, val);
			}

			cayenneExp.setParams(paramsMap);
		}

		return cayenneExp;
	}

	private CayenneExp processArray(JsonNode array) {

		int len = array.size();
		if (len < 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "array 'cayenneExp' mist have at least one element");
		}

		String expString = array.get(0).asText();

		CayenneExp cayenneExp = new CayenneExp();
		cayenneExp.setExp(expString);
		if (len < 2) {
			return cayenneExp;
		}

		Object[] params = new Object[len - 1];

		for (int i = 1; i < len; i++) {

			JsonNode paramNode = array.get(i);
			params[i - 1] = extractValue(paramNode);
		}
		cayenneExp.setParams(params);

		return cayenneExp;
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
