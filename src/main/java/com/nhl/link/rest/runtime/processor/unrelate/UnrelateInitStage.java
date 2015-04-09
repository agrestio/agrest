package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class UnrelateInitStage extends ProcessingStage<UnrelateContext<?>> {

	public UnrelateInitStage(Processor<UnrelateContext<?>> next) {
		super(next);
	}

	@Override
	protected void doExecute(UnrelateContext<?> context) {
		context.setResponse(new SimpleResponse(true));
	}

}