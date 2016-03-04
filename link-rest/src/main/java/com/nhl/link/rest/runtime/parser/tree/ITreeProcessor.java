package com.nhl.link.rest.runtime.parser.tree;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.ResourceEntity;

/**
 * Processes include/exclude property keys from the request, constructing a
 * matching response entity hierarchy.
 * 
 * @since 1.5
 */
public interface ITreeProcessor {

	void process(ResourceEntity<?> entity, UriInfo uriInfo);
}
