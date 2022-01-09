package io.agrest.cayenne.qualifier;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;

/**
 * @since 3.3
 */
public interface IQualifierParser {

    Expression parse(Exp qualifier);
}
