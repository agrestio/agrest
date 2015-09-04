package com.nhl.link.rest.runtime.processor.update;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class ParseUpdateRequestStage<T> extends ProcessingStage<UpdateContext<T>, T> {

	private IRequestParser requestParser;

	public ParseUpdateRequestStage(Processor<UpdateContext<T>, ? super T> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}

	@Override
	protected Class<? extends Annotation> afterStageListener() {
		return UpdateRequestParsed.class;
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {
		requestParser.parseUpdate(context);
	}
}
