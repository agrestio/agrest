package io.agrest.runtime.cayenne.converter;

import io.agrest.backend.util.converter.ExpressionMatcher;
import org.apache.cayenne.exp.Expression;

/**
 *
 *
 */
public class CayenneExpressionMatcher implements ExpressionMatcher<Expression> {


    @Override
    public Boolean apply(Expression expression, Object o) {
        return expression.match(o);
    }
}
