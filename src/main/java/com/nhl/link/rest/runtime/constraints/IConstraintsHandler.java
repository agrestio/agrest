package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

	/**
	 * Applies constraints to the {@link DataResponse}, potentially filtering
	 * out some properties from the response.
	 */
	<T> void constrainResponse(DataResponse<T> response, SizeConstraints sizeConstraints,
			TreeConstraints<T> readConstraints);

	/**
	 * Applies constraints to the {@link UpdateResponse}, potentially filtering
	 * out updates for certain properties.
	 */
	<T> void constrainUpdate(UpdateResponse<T> response, TreeConstraints<T> writeConstraints);
}
