package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class UpdateApplyRequestStage extends ProcessingStage<UpdateContext<?>> {

	private IRequestParser requestParser;

	public UpdateApplyRequestStage(Processor<UpdateContext<?>> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}

	@Override
	protected void doExecute(UpdateContext<?> context) {
		requestParser.parseUpdate(context.getResponse(), context.getUriInfo(), context.getEntityData());
	}
}
