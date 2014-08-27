package com.nhl.link.rest.runtime.parser.tree;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;

/**
 * @since 1.5
 */
public interface ITreeProcessor {

	void process(DataResponse<?> response, UriInfo uriInfo);
}
