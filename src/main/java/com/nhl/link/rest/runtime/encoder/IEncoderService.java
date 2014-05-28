package com.nhl.link.rest.runtime.encoder;

import javax.ws.rs.core.Response.ResponseBuilder;

import com.nhl.link.rest.DataResponse;

public interface IEncoderService {

	/**
	 * Builds a hierarchical encoder based on the specified
	 * {@link ResponseBuilder} and initializes the builder with this encoder.
	 */
	<T> DataResponse<T> makeEncoder(DataResponse<T> response);
}
