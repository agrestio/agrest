package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

public class CayenneExpProcessor implements ICayenneExpProcessor {

	private CayenneExpProcessorWorker worker;
	private ExpressionPostProcessor postProcessor;

	public CayenneExpProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		this.worker = new CayenneExpProcessorWorker(jsonParser);
		postProcessor = new ExpressionPostProcessor(pathCache);
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
}
