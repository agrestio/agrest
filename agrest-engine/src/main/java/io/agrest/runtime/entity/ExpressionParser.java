package io.agrest.runtime.entity;

import io.agrest.base.protocol.CayenneExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.List;
import java.util.Map;

/**
 * @since 3.3
 */
public class ExpressionParser implements IExpressionParser {

    @Override
    public Expression parse(CayenneExp cayenneExp) {

        if (cayenneExp == null) {
            return null;
        }

        String exp = cayenneExp.getExp();
        if (exp == null || exp.isEmpty()) {
            return null;
        }

        List<Object> inPositionParams = cayenneExp.getInPositionParams();
        if (inPositionParams != null && !inPositionParams.isEmpty()) {
            return ExpressionFactory.exp(exp, inPositionParams.toArray());
        }

        Expression expression = ExpressionFactory.exp(exp);

        Map<String, Object> params = cayenneExp.getParams();
        if (params != null && !params.isEmpty()) {
            expression = expression.params(params);
        }

        return expression;
    }
}
