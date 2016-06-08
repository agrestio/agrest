package com.nhl.link.rest.multisource;

import java.util.Objects;

import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A LinkRest listener that captures {@link SelectContext} early during LR chain
 * execution and exposes is outside of request chain. One common use case is to
 * provide access to the parsed request objects to the subordinate fetchers.
 * 
 * <p>
 * For internal use.
 * 
 * @since 2.0
 */
public class SelectContextSource<T> {

	private SelectContext<T> context;

	// capture mutable context as soon as we can
	@SelectChainInitialized
	public void captureContext(SelectContext<T> context) {
		this.context = context;
	}

	public SelectContext<T> getContext() {
		return Objects.requireNonNull(context, "context event was not received, context is null");
	}
}