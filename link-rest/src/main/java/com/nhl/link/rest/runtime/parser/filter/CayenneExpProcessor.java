package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

public class CayenneExpProcessor implements ICayenneExpProcessor {

	private CayenneExpProcessorWorker worker;
	private IExpressionPostProcessor postProcessor;

	public CayenneExpProcessor(@Inject IJacksonService jsonParser, @Inject IExpressionPostProcessor postProcessor) {
		this.worker = new CayenneExpProcessorWorker(jsonParser);
		this.postProcessor = postProcessor;
	}

	@Override
	public Expression process(LrEntity<?> entity, String expressionString) {

		if (expressionString == null || expressionString.length() == 0) {
			return null;
		}

		return postProcessor.process(entity, worker.exp(expressionString));
	}

	@Override
	public Expression process(LrEntity<?> entity, JsonNode expressionNode) {

		if (expressionNode == null) {
			return null;
		}

		return postProcessor.process(entity, worker.exp(expressionNode));
	}

	/**
	 * @since 2.13
	 */
	@Override
	public Expression process(LrEntity<?> entity, CayenneExp expressionParam) {
		if (expressionParam == null) {
			return null;
		}

		return postProcessor.process(entity, worker.exp(expressionParam));
	}
}
