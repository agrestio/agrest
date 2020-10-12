package io.agrest.cayenne.qualifier;

import io.agrest.base.protocol.CayenneExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.3
 */
public class QualifierParser implements IQualifierParser {

    @Override
    public Expression parse(List<CayenneExp> qualifiers) {

        switch (qualifiers.size()) {
            case 0:
                return null;
            case 1:
                return parse(qualifiers.get(0));
            default:
                List<Expression> expressions = new ArrayList<>(qualifiers.size());
                qualifiers.forEach(q -> expressions.add(parse(q)));
                return ExpressionFactory.and(expressions);
        }
    }

    protected Expression parse(CayenneExp qualifier) {

        Expression exp = ExpressionFactory.exp(qualifier.getExp());

        if (qualifier.usesPositionalParameters()) {
            return exp.paramsArray(qualifier.getPositionalParams());
        } else if (qualifier.usesNamedParameters()) {
            return exp.params(qualifier.getNamedParams());
        } else {
            return exp;
        }
    }
}
