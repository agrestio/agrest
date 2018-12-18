package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.CayenneExp;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.List;
import java.util.Map;

/**
 * @since 2.13
 */
public class CayenneExpMerger implements IAgExpMerger<Expression> {

	private IExpressionPostProcessor<Expression> postProcessor;

	public CayenneExpMerger(@Inject IExpressionPostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	/**
	 * @since 2.13
	 */
	@Override
	public void merge(ResourceEntity<?, Expression> resourceEntity, CayenneExp cayenneExp) {
		Expression exp = postProcessor.process(resourceEntity.getAgEntity(), exp(cayenneExp));
		if (exp != null) {
			resourceEntity.andQualifier(exp);
		}
	}


	/**
	 * @since 2.13
	 */
	private Expression exp(CayenneExp cayenneExp) {
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
