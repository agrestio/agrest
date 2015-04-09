package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.16
 */
public class SelectInitStage extends ProcessingStage<SelectContext<?>> {

	public SelectInitStage(Processor<SelectContext<?>> next) {
		super(next);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doExecute(SelectContext<?> context) {
		DataResponse response = DataResponse.forType(context.getType());
		context.setResponse(response);
	}
}
