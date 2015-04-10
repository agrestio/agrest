package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class DeleteInitStage<T> extends ProcessingStage<DeleteContext<T>, T> {

	public DeleteInitStage(Processor<DeleteContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(DeleteContext<T> context) {
		context.setResponse(new SimpleResponse(true));
	}

}