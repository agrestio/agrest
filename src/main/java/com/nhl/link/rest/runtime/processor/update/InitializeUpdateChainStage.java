package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class InitializeUpdateChainStage<T> extends ProcessingStage<UpdateContext<T>, T> {

	public InitializeUpdateChainStage(Processor<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		UpdateResponse<T> response = new UpdateResponse<>(context.getType());
		context.setResponse(response);
	}

}
