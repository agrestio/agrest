package com.nhl.link.rest.runtime.processor.update;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.DataResponse;
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
		// TODO: the actual response may be SimpleResponse ... still need to
		// maintain an illusion of a DataResponse, as it holds the parameters
		// that we need for update . E.g. ResourceEntity (it has qualifier,
		// etc.)
		DataResponse<T> response = DataResponse.forType(context.getType());
		context.setResponse(response);
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return UpdateChainInitialized.class;
	}

}
