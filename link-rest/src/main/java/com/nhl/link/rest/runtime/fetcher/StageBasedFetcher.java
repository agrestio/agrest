package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 2.0
 */
public class StageBasedFetcher<T,P> implements Fetcher<T,P> {

	@SuppressWarnings("rawtypes")
	private ProcessingStage pipeline;

	public StageBasedFetcher(ProcessingStage<?, ?> pipeline) {
		this.pipeline = pipeline;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<T> fetch(SelectContext<T> context, Iterable<P> parents) {
		ChainProcessor.execute(pipeline, context);
		return context.getObjects();
	}
}
