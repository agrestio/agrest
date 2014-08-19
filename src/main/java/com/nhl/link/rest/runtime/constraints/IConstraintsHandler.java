package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.TreeConstraints;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

	/**
	 * Applies constraints to the {@link DataResponse}.
	 */
	void apply(DataResponse<?> target, SizeConstraints sizeConstraints, TreeConstraints treeConstraints);
}
