package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.19
 */
public class InitializeSelectChainStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	public InitializeSelectChainStage(ProcessingStage<SelectContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return SelectChainInitialized.class;
	}
	
	@Override
	protected void doExecute(SelectContext<T> context) {
		DataResponse<T> response = DataResponse.forType(context.getType());
		context.setResponse(response);
	}
}
