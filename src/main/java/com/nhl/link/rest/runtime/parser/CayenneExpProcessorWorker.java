package com.nhl.link.rest.runtime.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.TraversalHandler;
import org.apache.cayenne.exp.TraversalHelper;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.parser.ConditionNode;
import org.apache.cayenne.exp.parser.SimpleNode;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.converter.ValueConverter;

class CayenneExpProcessorWorker {

	private static final String EXP = "exp";
	private static final String PARAMS = "params";
	private static final int MAX_EXP_LENGTH = 1024;

	private JsonNode expNode;
	private JsonNode paramsNode;
	private EntityPathCache entityPathCache;
	private Map<String, ValueConverter> converters;
	private TraversalHandler expressionPostProcessor;

	CayenneExpProcessorWorker(JsonNode rootNode, Map<String, ValueConverter> converters, EntityPathCache entityPathCache) {

		this.expNode = rootNode.get(EXP);
		this.paramsNode = rootNode.get(PARAMS);
		this.converters = converters;
		this.entityPathCache = entityPathCache;
		this.expressionPostProcessor = new ExpressionPostProcessor();
	}

	Expression exp() {
		if (expNode == null) {
			return null;
		}

		String expString = expNode.asText();

		// sanity check before parsing...
		checkExpressionLength(expString);

		Expression exp = Expression.fromString(expString);
		return validateAndBindParams(exp);
	}

	private Expression validateAndBindParams(Expression exp) {

		if (paramsNode != null) {
			Map<String, Object> parsedParams = new HashMap<>();

			Iterator<String> it = paramsNode.fieldNames();
			while (it.hasNext()) {
				String key = it.next();
				JsonNode valueNode = paramsNode.get(key);
				Object value = extractValue(valueNode);
				parsedParams.put(key, value);
			}

			exp = exp.expWithParameters(parsedParams);
		}

		// change expression in-place
		// note - this will not fully handle an expression whose root is
		// ASTObjPath, so will manually process it below
		exp.traverse(expressionPostProcessor);

		// process root ASTObjPath that can't be properly handled by
		// 'expressionPostProcessor'. If it happens to be "id", it will be
		// converted to "db:id".
		if (exp instanceof ASTObjPath) {
			exp = entityPathCache.getPathDescriptor((ASTObjPath) exp).getPathExp();
		}

		return exp;
	}

	private Object convert(SimpleNode parentExp, DeferredConvertionWrapper wrapper) {

		ASTObjPath peerPath = findPeerPath(parentExp, wrapper);

		if (peerPath != null) {

			PathDescriptor pd = entityPathCache.getPathDescriptor(peerPath);
			if (pd.isAttribute()) {
				ValueConverter converter = converters.get(pd.getType());
				if (converter != null) {

					try {
						return converter.value(wrapper.value);
					} catch (Exception e) {
						throw new LinkRestException(Status.BAD_REQUEST,
								"cayenneExp.params contains an incorrectly formatted value: '" + wrapper.value + "'");
					}
				}
			}
		}

		return wrapper.value;
	}

	private ASTObjPath findPeerPath(SimpleNode exp, Object child) {

		if (exp == null) {
			return null;
		}

		if (!(exp instanceof ConditionNode)) {
			return findPeerPath((SimpleNode) exp.jjtGetParent(), exp);
		}

		// terminate walk up at a ConditionNode, start a walk down
		int len = exp.getOperandCount();
		for (int i = 0; i < len; i++) {
			Object operand = exp.getOperand(i);
			if (operand == child || !(operand instanceof Expression)) {
				continue;
			}

			ASTObjPath path = findChildPath((Expression) operand);
			if (path != null) {
				return path;
			}
		}

		return null;
	}

	private ASTObjPath findChildPath(Expression exp) {
		if (exp instanceof ASTObjPath) {
			return (ASTObjPath) exp;
		}

		int len = exp.getOperandCount();
		for (int i = 0; i < len; i++) {
			Object operand = exp.getOperand(i);
			if (!(operand instanceof Expression)) {
				continue;
			}

			ASTObjPath path = findChildPath((Expression) operand);
			if (path != null) {
				return path;
			}
		}

		return null;
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
			return new DeferredConvertionWrapper(valueNode.asText());
		}
	}

	private static List<Object> extractArray(JsonNode arrayNode) {

		List<Object> values = new ArrayList<>(arrayNode.size());
		for (JsonNode value : arrayNode) {
			values.add(extractValue(value));
		}

		return values;
	}

	private static void checkExpressionLength(String exp) {
		if (exp.length() > MAX_EXP_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "cayenneExp.exp is to long: " + exp);
		}
	}

	private static class DeferredConvertionWrapper {
		private String value;

		DeferredConvertionWrapper(String value) {
			this.value = value;
		}
	}

	private class ExpressionPostProcessor extends TraversalHelper {

		@Override
		public void startNode(Expression node, Expression parentNode) {
			if (node instanceof ASTDbPath) {
				// probably a good idea to disallow DbPath's
				throw new LinkRestException(Status.BAD_REQUEST,
						"cayenneExp.exp contains a DB_PATH expression that is not allowed here: " + parentNode);
			}

		}

		@Override
		public void finishedChild(Expression parentNode, int childIndex, boolean hasMoreChildren) {

			Object childNode = parentNode.getOperand(childIndex);
			if (childNode instanceof ASTObjPath) {
				
				// validate and replace if needed ... note that we can only
				// replace non-root nodes during the traversal. Root node is
				// validated and replaced explicitly by the caller.
				ASTPath replacement = entityPathCache.getPathDescriptor((ASTObjPath) childNode).getPathExp();
				if (replacement != childNode) {
					parentNode.setOperand(childIndex, replacement);
				}
			}
		}

		@Override
		public void objectNode(Object leaf, Expression parentNode) {
			if (leaf instanceof DeferredConvertionWrapper) {
				for (int i = 0; i < parentNode.getOperandCount(); i++) {
					if (leaf == parentNode.getOperand(i)) {
						parentNode.setOperand(i, convert((SimpleNode) parentNode, (DeferredConvertionWrapper) leaf));
					}
				}
			}
			// this is ASTList child case
			else if (leaf instanceof Object[]) {

				Object[] array = (Object[]) leaf;
				for (int i = 0; i < array.length; i++) {
					if (array[i] instanceof DeferredConvertionWrapper) {
						array[i] = convert((SimpleNode) parentNode, (DeferredConvertionWrapper) array[i]);
					}
				}
			}
		}

	}
}
