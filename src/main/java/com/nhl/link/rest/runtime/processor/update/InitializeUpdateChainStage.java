package com.nhl.link.rest.runtime.processor.update;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.16
 */
public class InitializeUpdateChainStage<T> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	public InitializeUpdateChainStage(ProcessingStage<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		UpdateResponse<T> response = new UpdateResponse<>(context.getType());
		context.setResponse(response);
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return UpdateChainInitialized.class;
	}

}
