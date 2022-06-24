package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;

/**
 * @since 3.3
 */
public class CayenneExpParser implements ICayenneExpParser {

    @Override
    public Expression parse(Exp qualifier) {
        if (qualifier == null) {
            return null;
        }

        CayenneExpressionVisitor visitor = new CayenneExpressionVisitor();
        return qualifier.accept(visitor, null);
    }
}
