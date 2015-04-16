package com.nhl.link.rest.runtime.parser.filter;

import org.apache.cayenne.exp.Expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.17
 */
public interface ICayenneExpProcessor {

	Expression process(LrEntity<?> entity, String expressionString);

	Expression process(LrEntity<?> entity, JsonNode expressionNode);
}
