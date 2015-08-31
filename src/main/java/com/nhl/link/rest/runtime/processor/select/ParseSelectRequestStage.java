package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.annotation.SelectRequestParsed;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.19
 */
public class ParseSelectRequestStage<T> extends ProcessingStage<SelectContext<T>, T> {

	private IRequestParser requestParser;

	public ParseSelectRequestStage(Processor<SelectContext<T>, ? super T> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}
	
	@Override
	protected Class<? extends Annotation> afterStageListener() {
		return SelectRequestParsed.class;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		requestParser.parseSelect(context);
	}
}
