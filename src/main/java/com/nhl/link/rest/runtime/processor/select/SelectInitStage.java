package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class SelectInitStage<T> extends ProcessingStage<SelectContext<T>, T> {

	public SelectInitStage(Processor<SelectContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		DataResponse<T> response = DataResponse.forType(context.getType());
		context.setResponse(response);
	}
}
