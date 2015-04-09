package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class DeleteInitStage extends ProcessingStage<DeleteContext<?>> {

	public DeleteInitStage(Processor<DeleteContext<?>> next) {
		super(next);
	}

	@Override
	protected void doExecute(DeleteContext<?> context) {
		context.setResponse(new SimpleResponse(true));
	}

}