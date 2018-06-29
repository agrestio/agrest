package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class CayenneExpProcessorWorker {

	private static final String EXP = "exp";
	private static final String PARAMS = "params";

	private IJacksonService jsonParser;

	CayenneExpProcessorWorker(IJacksonService jsonParser) {
		this.jsonParser = jsonParser;
	}

	Expression exp(String cayenneExp) {

		if (cayenneExp == null) {
			return null;
		}

		Expression exp;

		if (cayenneExp.startsWith("[")) {
			exp = processArray(jsonParser.parseJson(cayenneExp));
		} else if (cayenneExp.startsWith("{")) {
			exp = processMap(jsonParser.parseJson(cayenneExp));
		} else {
			exp = processExp(cayenneExp);
		}

		return exp;
	}

	Expression exp(JsonNode cayenneExp) {
		if (cayenneExp == null) {
			return null;
		}

		Expression exp;

		if (cayenneExp.isArray()) {
			exp = processArray(cayenneExp);
		} else if (cayenneExp.isObject()) {
			exp = processMap(cayenneExp);
		} else {
			exp = processExp(cayenneExp.asText());
		}

		return exp;
	}

	/**
	 * @since 2.13
	 */
	Expression exp(CayenneExp cayenneExp) {
		if (cayenneExp == null) {
			return null;
		}

		Expression exp = ExpressionFactory.exp(cayenneExp.getExp());
		exp = exp.params(cayenneExp.getParams());

		return exp;
	}

	private Expression processExp(String cayenneExp) {
		return ExpressionFactory.exp(cayenneExp);
	}

	private Expression processMap(JsonNode map) {

		// 'exp' key is required; 'params' key is optional
		JsonNode expNode = map.get(EXP);
		if (expNode == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "'exp' key is missing in 'cayenneExp' map");
		}

		Expression exp = ExpressionFactory.exp(expNode.asText());

		JsonNode paramsNode = map.get(PARAMS);
		if (paramsNode != null) {

			Map<String, Object> paramsMap = new HashMap<>();

			Iterator<String> it = paramsNode.fieldNames();
			while (it.hasNext()) {
				String key = it.next();
				JsonNode valueNode = paramsNode.get(key);
				Object value = extractValue(valueNode);
				paramsMap.put(key, value);
			}

			exp = exp.params(paramsMap);
		}

		return exp;
	}

	private Expression processArray(JsonNode array) {

		int len = array.size();
		if (len < 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "array 'cayenneExp' mist have at least one element");
		}

		String expString = array.get(0).asText();
		if (len < 2) {
			return ExpressionFactory.exp(expString);
		}

		Object[] params = new Object[len - 1];

		for (int i = 1; i < len; i++) {

			JsonNode paramNode = array.get(i);
			params[i - 1] = extractValue(paramNode);
		}

		return ExpressionFactory.exp(expString, params);
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
