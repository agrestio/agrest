package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class UpdateApplyRequestStage<T> extends ProcessingStage<UpdateContext<T>, T> {

	private IRequestParser requestParser;

	public UpdateApplyRequestStage(Processor<UpdateContext<T>, ? super T> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		requestParser.parseUpdate(context);
	}
}
