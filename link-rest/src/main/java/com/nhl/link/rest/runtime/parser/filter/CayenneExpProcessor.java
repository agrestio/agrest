package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.List;
import java.util.Map;

public class CayenneExpProcessor implements ICayenneExpProcessor {

	private CayenneExpConverter converter;
	private IExpressionPostProcessor postProcessor;

	public CayenneExpProcessor(@Inject IJacksonService jsonParser, @Inject IExpressionPostProcessor postProcessor) {
		this.converter = new CayenneExpConverter(jsonParser);
		this.postProcessor = postProcessor;
	}

	@Override
	public void process(ResourceEntity<?> resourceEntity, String expressionString) {

		CayenneExp cayenneExp = converter.fromString(expressionString);
		Expression exp = postProcessor.process(resourceEntity.getLrEntity(), exp(cayenneExp));
		resourceEntity.andQualifier(exp);
	}


	/**
	 * @since 2.13
	 */
	@Override
	public void process(ResourceEntity<?> resourceEntity, Query query) {

		Expression exp = postProcessor.process(resourceEntity.getLrEntity(), exp(query.getCayenneExp()));
		resourceEntity.andQualifier(exp);
	}

	/**
	 * @since 2.13
	 */
	@Override
	public CayenneExpConverter getConverter() {
		return converter;
	}


	/**
	 * @since 2.13
	 */
	Expression exp(CayenneExp cayenneExp) {
		if (cayenneExp == null) {
			return null;
		}
		
		final String exp = cayenneExp.getExp();
        if (exp == null || exp.isEmpty()) {
            return null;
        }

        final List<Object> inPositionParams = cayenneExp.getInPositionParams();
        if (inPositionParams != null && !inPositionParams.isEmpty()) {
            return ExpressionFactory.exp(exp, inPositionParams.toArray());
        }

        Expression expression = ExpressionFactory.exp(exp);

		final Map<String, Object> params = cayenneExp.getParams();
        if (params != null && !params.isEmpty()) {
            expression = expression.params(params);
        }

        return expression;
	}
}
