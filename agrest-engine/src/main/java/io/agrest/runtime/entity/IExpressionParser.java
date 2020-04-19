package io.agrest.runtime.entity;

import io.agrest.base.protocol.CayenneExp;
import org.apache.cayenne.exp.Expression;

/**
 * @since 3.3
 */
public interface IExpressionParser {

    Expression parse(CayenneExp cayenneExp);
}
