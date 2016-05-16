package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 2.0
 */
public class StageBasedFetcher<T> implements Fetcher<T> {

	private ProcessingStage<SelectContext<T>, T> pipeline;

	@Override
	public Iterable<T> fetch(SelectContext<T> context) {
		ChainProcessor.execute(pipeline, context);
		return context.getEntity().getObjects();
	}
}
