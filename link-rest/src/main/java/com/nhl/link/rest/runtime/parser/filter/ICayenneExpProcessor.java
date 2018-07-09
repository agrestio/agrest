package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 1.17
 */
public interface ICayenneExpProcessor extends IQueryProcessor {

	void process(ResourceEntity<?> resourceEntity, String expressionString);

	void process(ResourceEntity<?> resourceEntity, JsonNode expressionNode);

}
