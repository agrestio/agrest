package com.nhl.link.rest.runtime.parser.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import javax.ws.rs.ext.ParamConverter;

public class CayenneExpProcessor implements ICayenneExpProcessor {

	private CayenneExpConverter converter;
	private IExpressionPostProcessor postProcessor;

	public CayenneExpProcessor(@Inject IJacksonService jsonParser, @Inject IExpressionPostProcessor postProcessor) {
		this.converter = new CayenneExpConverter(jsonParser);
		this.postProcessor = postProcessor;
	}

	@Override
	public void process(ResourceEntity<?> resourceEntity, String expressionString) {

		Expression exp = postProcessor.process(resourceEntity.getLrEntity(), converter.exp(expressionString));
		resourceEntity.andQualifier(exp);
	}

	@Override
	public void process(ResourceEntity<?> resourceEntity, JsonNode expressionNode) {

		Expression exp = postProcessor.process(resourceEntity.getLrEntity(), converter.exp(expressionNode));
		resourceEntity.andQualifier(exp);
	}

	/**
	 * @since 2.13
	 */
	@Override
	public void process(ResourceEntity<?> resourceEntity, Query query) {

		Expression exp = postProcessor.process(resourceEntity.getLrEntity(), converter.exp(query.getCayenneExp()));
		resourceEntity.andQualifier(exp);
	}

	/**
	 * @since 2.13
	 */
	@Override
	public ParamConverter<CayenneExp> getConverter() {
		return converter;
	}
}
