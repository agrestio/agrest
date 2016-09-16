package com.nhl.link.rest.runtime.adapter.sencha;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.FilterUtil;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTObjPath;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

import static org.apache.cayenne.exp.ExpressionFactory.expFalse;
import static org.apache.cayenne.exp.ExpressionFactory.greaterExp;
import static org.apache.cayenne.exp.ExpressionFactory.greaterOrEqualExp;
import static org.apache.cayenne.exp.ExpressionFactory.inExp;
import static org.apache.cayenne.exp.ExpressionFactory.lessExp;
import static org.apache.cayenne.exp.ExpressionFactory.lessOrEqualExp;
import static org.apache.cayenne.exp.ExpressionFactory.likeIgnoreCaseExp;
import static org.apache.cayenne.exp.ExpressionFactory.matchExp;
import static org.apache.cayenne.exp.ExpressionFactory.noMatchExp;

public class SenchaFilterProcessor implements ISenchaFilterProcessor {

	private static final int MAX_VALUE_LENGTH = 1024;
	private static final String EXACT_MATCH = "exactMatch";
	private static final String PROPERTY = "property";
	private static final String VALUE = "value";
	private static final String DISABLED = "disabled";
	private static final String OPERATOR = "operator";

	private IJacksonService jsonParser;
	private IPathCache pathCache;
	private ExpressionPostProcessor postProcessor;

	public SenchaFilterProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;
		this.postProcessor = new ExpressionPostProcessor(pathCache);
	}

	@Override
	public Expression process(LrEntity<?> entity, String filtersJson) {

		if (filtersJson == null || filtersJson.length() == 0) {
			return null;
		}

		JsonNode rootNode = jsonParser.parseJson(filtersJson);
		if (rootNode == null) {
			return null;
		}

		Expression combined = null;

		for (JsonNode filterNode : rootNode) {
			JsonNode propertyNode = filterNode.get(PROPERTY);
			if (propertyNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'property' is missing" + filterNode.asText());
			}

			JsonNode valueNode = filterNode.get(VALUE);
			if (valueNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is missing" + filterNode.asText());
			}

			JsonNode disabledNode = filterNode.get(DISABLED);
			if (disabledNode != null && disabledNode.asBoolean()) {
				continue;
			}

			String property = propertyNode.asText();

			Object value = extractValue(valueNode);

			// note that 'exactMatch' is ignored everywhere but in a like
			// expression
			boolean exactMatch = false;
			JsonNode exactMatchNode = filterNode.get(EXACT_MATCH);
			if (exactMatchNode != null) {
				exactMatch = exactMatchNode.asBoolean();
			}

			JsonNode operatorNode = filterNode.get(OPERATOR);
			String operator = (operatorNode != null) ? operatorNode.asText() : "like";

			Expression qualifier;
			switch (operator) {
			case "like":
				qualifier = like(property, value, exactMatch);
				break;
			case "=":
				qualifier = eq(property, value);
				break;
			case "!=":
				qualifier = neq(property, value);
				break;
			case ">":
				qualifier = gt(property, value);
				break;
			case ">=":
				qualifier = gte(property, value);
				break;
			case "<":
				qualifier = lt(property, value);
				break;
			case "<=":
				qualifier = lte(property, value);
				break;
			case "in":
				qualifier = in(property, value);
				break;
			default:
				throw new LinkRestException(Status.BAD_REQUEST, "Invalid filter operator: " + operator);
			}

			// validate property path
			if (qualifier.getOperandCount() == 2) {
				pathCache.getPathDescriptor(entity, (ASTObjPath) qualifier.getOperand(0));
			}

			combined = combined != null ? combined.andExp(qualifier) : qualifier;
		}

		return postProcessor.process(entity, combined);
	}

	Expression eq(String property, Object value) {
		return matchExp(property, value);
	}

	Expression neq(String property, Object value) {
		return noMatchExp(property, value);
	}

	Expression like(String property, Object value, boolean exactMatch) {
		if (value == null || exactMatch || value instanceof Boolean) {
			return eq(property, value);
		}

		String string = value.toString();
		checkValueLength(string);
		string = FilterUtil.escapeValueForLike(string) + "%";
		return likeIgnoreCaseExp(property, string);
	}

	Expression gt(String property, Object value) {
		return (value == null) ? expFalse() : greaterExp(property, value);
	}

	Expression gte(String property, Object value) {
		return (value == null) ? expFalse() : greaterOrEqualExp(property, value);
	}

	Expression lt(String property, Object value) {
		return (value == null) ? expFalse() : lessExp(property, value);
	}

	Expression lte(String property, Object value) {
		return (value == null) ? expFalse() : lessOrEqualExp(property, value);
	}

	@SuppressWarnings("rawtypes")
	Expression in(String property, Object value) {

		if (!(value instanceof List)) {
			return eq(property, value);
		}

		return inExp(property, (List) value);
	}

	private static Object extractValue(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

		// ExtJS converts everything to String except for NULL and booleans. So
		// follow the same logic here...
		// (http://docs.sencha.com/extjs/4.1.2/source/Filter.html#Ext-util-Filter)
		switch (type) {
		case VALUE_NULL:
			return null;
		case VALUE_FALSE:
			return false;
		case VALUE_TRUE:
			return true;
		case VALUE_NUMBER_INT:
			return valueNode.asInt();
		case VALUE_NUMBER_FLOAT:
			return valueNode.asDouble();
		case START_ARRAY:
			return extractArray(valueNode);
		default:
			return valueNode.asText();
		}
	}

	private static List<Object> extractArray(JsonNode arrayNode) {

		List<Object> values = new ArrayList<>(arrayNode.size());
		for (JsonNode value : arrayNode) {
			values.add(extractValue(value));
		}

		return values;
	}

	private void checkValueLength(String value) {
		if (value.length() > MAX_VALUE_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is to long: " + value);
		}
	}
}
