package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class UpdateInitStage extends ProcessingStage<UpdateContext<?>> {

	public UpdateInitStage(Processor<UpdateContext<?>> next) {
		super(next);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doExecute(UpdateContext<?> context) {
		UpdateResponse response = new UpdateResponse(context.getType());
		context.setResponse(response);
	}

}
