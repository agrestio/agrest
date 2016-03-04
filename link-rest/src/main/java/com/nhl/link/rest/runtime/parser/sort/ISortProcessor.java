package com.nhl.link.rest.runtime.parser.sort;

import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;

/**
 * @since 1.5
 */
public interface ISortProcessor {

	void process(ResourceEntity<?> entity, UriInfo uriInfo);

	void process(ResourceEntity<?> entity, JsonNode sortNode);
}
