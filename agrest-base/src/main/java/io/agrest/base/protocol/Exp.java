package io.agrest.base.protocol;

import io.agrest.base.protocol.exp.*;

import java.util.Map;

/**
 * Represents {@link AgProtocol#exp} protocol parameter.
 *
 * @since 4.4
 */
public interface Exp {

    static Exp simple(String template) {
        return new SimpleExp(template);
    }

    static Exp withPositionalParams(String template, Object... params) {
        return params.length == 0 ? simple(template) : new PositionalParamsExp(template, params);
    }

    static Exp withNamedParams(String template, Map<String, Object> params) {
        return params.isEmpty() ? simple(template) : new NamedParamsExp(template, params);
    }

    void visit(ExpVisitor visitor);

    default Exp and(Exp exp) {
        return exp != null ? new CompositeExp(CompositeExp.AND, this, exp) : this;
    }

    default Exp or(Exp exp) {
        return exp != null ? new CompositeExp(CompositeExp.OR, this, exp) : this;
    }
}
