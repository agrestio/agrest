package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConstraints;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

	/**
	 * Applies constraints to the {@link DataResponse}.
	 */
	void apply(DataResponseConstraints constraints, DataResponse<?> target);
}
