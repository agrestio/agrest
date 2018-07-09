package com.nhl.link.rest.runtime.query;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.13
 *
 * Represents Cayenne Expression query parameter
 */
public class CayenneExp {

    private String exp = null;
    private Map<String, Object> params = new HashMap<>();
    private Object[] inPositionParams;

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setParams(Object... params) {
        inPositionParams = params;
    }

    public Expression toExpression() {
        if (exp == null || exp.isEmpty()) {
            return null;
        }

        if (inPositionParams != null && inPositionParams.length > 0) {
            return ExpressionFactory.exp(exp, inPositionParams);
        }

        Expression expression = ExpressionFactory.exp(exp);

        if (params != null && !params.isEmpty()) {
            expression = expression.params(params);
        }

        return expression;
    }
}
