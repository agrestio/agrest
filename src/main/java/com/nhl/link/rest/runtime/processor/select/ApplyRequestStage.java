package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.16
 */
public class ApplyRequestStage<T> extends ProcessingStage<SelectContext<T>, T> {

	private IRequestParser requestParser;

	public ApplyRequestStage(Processor<SelectContext<T>, ? super T> next, IRequestParser requestParser) {
		super(next);
		this.requestParser = requestParser;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		requestParser.parseSelect(context);
	}
}
