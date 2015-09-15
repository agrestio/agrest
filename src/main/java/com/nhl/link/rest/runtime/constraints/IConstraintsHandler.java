package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.3
 */
public interface IConstraintsHandler {

	/**
	 * Applies constraints to the {@link ResourceEntity}, potentially filtering
	 * out some properties from the response.
	 */
	<T> void constrainResponse(ResourceEntity<T> entity, SizeConstraints sizeConstraints,
			ConstraintsBuilder<T> readConstraints);

	/**
	 * Applies constraints to the {@link UpdateContext}, potentially filtering
	 * out updates for certain properties.
	 */
	<T> void constrainUpdate(UpdateContext<T> context, ConstraintsBuilder<T> writeConstraints);
}
