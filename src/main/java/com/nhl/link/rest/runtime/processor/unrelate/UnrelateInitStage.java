package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.16
 */
public class UnrelateInitStage<Т> extends BaseLinearProcessingStage<UnrelateContext<Т>, Т> {

	public UnrelateInitStage(ProcessingStage<UnrelateContext<Т>, ? super Т> next) {
		super(next);
	}

	@Override
	protected void doExecute(UnrelateContext<Т> context) {
		context.setResponse(new SimpleResponse(true));
	}

}