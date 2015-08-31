package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.annotation.SelectChainInitialized;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.19
 */
public class InitializeSelectChainStage<T> extends ProcessingStage<SelectContext<T>, T> {

	public InitializeSelectChainStage(Processor<SelectContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected Class<? extends Annotation> afterStageListener() {
		return SelectChainInitialized.class;
	}
	
	@Override
	protected void doExecute(SelectContext<T> context) {
		DataResponse<T> response = DataResponse.forType(context.getType());
		context.setResponse(response);
	}
}
