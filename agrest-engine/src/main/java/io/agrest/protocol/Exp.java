package io.agrest.protocol;

import io.agrest.exp.CompositeExp;
import io.agrest.exp.ExpVisitor;
import io.agrest.exp.KeyValueExp;
import io.agrest.exp.NamedParamsExp;
import io.agrest.exp.PositionalParamsExp;
import io.agrest.exp.SimpleExp;

import java.util.Map;

/**
 * Represents {@link ControlParams#exp} protocol parameter.
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

    /**
     * @since 5.0
     */
    static Exp keyValue(String key, String op, Object value) {
        return new KeyValueExp(key, op, value);
    }

    /**
     * Invokes a callback on the visitor corresponding to one of the known expression types. The operation is
     * non-recursive even for composite expressions. If the visitor needs to descend into expression tree, it will
     * need to implement this logic on its own.
     */
    void visit(ExpVisitor visitor);

    default Exp and(Exp exp) {
        return exp != null ? new CompositeExp(CompositeExp.AND, this, exp) : this;
    }

    default Exp or(Exp exp) {
        return exp != null ? new CompositeExp(CompositeExp.OR, this, exp) : this;
    }
}
