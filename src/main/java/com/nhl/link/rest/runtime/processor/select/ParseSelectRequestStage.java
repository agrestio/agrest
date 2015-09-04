package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.19
 */
public class ParseSelectRequestStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private IRequestParser requestParser;

	public ParseSelectRequestStage(ProcessingStage<SelectContext<T>, ? super T> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}
	
	@Override
	public Class<? extends Annotation> afterStageListener() {
		return SelectRequestParsed.class;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		requestParser.parseSelect(context);
	}
}
