package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class UnrelateInitStage<Т> extends ProcessingStage<UnrelateContext<Т>, Т> {

	public UnrelateInitStage(Processor<UnrelateContext<Т>, ? super Т> next) {
		super(next);
	}

	@Override
	protected void doExecute(UnrelateContext<Т> context) {
		context.setResponse(new SimpleResponse(true));
	}

}