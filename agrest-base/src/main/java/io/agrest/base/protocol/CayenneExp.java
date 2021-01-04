package io.agrest.base.protocol;

import io.agrest.base.protocol.exp.*;

import java.util.Map;

/**
 * Represents 'cayenneExp' Agrest protocol parameter.
 *
 * @since 3.8
 */
public interface CayenneExp {


    static CayenneExp simple(String template) {
        return new SimpleExp(template);
    }

    static CayenneExp withPositionalParams(String template, Object... params) {
        return params.length == 0 ? simple(template) : new PositionalParamsExp(template, params);
    }

    static CayenneExp withNamedParams(String template, Map<String, Object> params) {
        return params.isEmpty() ? simple(template) : new NamedParamsExp(template, params);
    }

    void visit(ExpVisitor visitor);

    default CayenneExp and(CayenneExp exp) {
        return exp != null ? new CompositeExp(CompositeExp.AND, this, exp) : this;
    }

    default CayenneExp or(CayenneExp exp) {
        return exp != null ? new CompositeExp(CompositeExp.OR, this, exp) : this;
    }
}
