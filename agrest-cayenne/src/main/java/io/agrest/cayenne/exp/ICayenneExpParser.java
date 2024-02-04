package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;

/**
 * @since 3.3
 */
public interface ICayenneExpParser {

    Expression parse(Exp exp);
}
