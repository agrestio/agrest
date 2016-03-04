package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.16
 */
public class DeleteInitStage<T> extends BaseLinearProcessingStage<DeleteContext<T>, T> {

	public DeleteInitStage(ProcessingStage<DeleteContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(DeleteContext<T> context) {
		context.setResponse(new SimpleResponse(true));
	}

}