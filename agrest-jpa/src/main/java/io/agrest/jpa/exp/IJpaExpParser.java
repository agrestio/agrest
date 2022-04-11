package io.agrest.jpa.exp;

import io.agrest.protocol.Exp;

/**
 * @since 5.0
 */
public interface IJpaExpParser {
    JpaExpression parse(Exp qualifier);
}
