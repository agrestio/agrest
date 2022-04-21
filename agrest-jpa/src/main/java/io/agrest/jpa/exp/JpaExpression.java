package io.agrest.jpa.exp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 5.0
 */
public class JpaExpression {

    private final String exp;

    private List<Object> params;

    public JpaExpression(String exp) {
        this.exp = exp;
    }

    public void addParameter(Object parameter) {
        if(params == null) {
            params = new ArrayList<>();
        }
        params.add(parameter);
    }

    public List<Object> getParams() {
        return params == null ? Collections.emptyList() : params;
    }

    public String getExp() {
        return exp;
    }

    public boolean isEmpty() {
        return exp == null || exp.length() == 0;
    }

    public JpaExpression and(JpaExpression expression) {
        if(expression.isEmpty()) {
            return this;
        }

        JpaExpression newExp = new JpaExpression(exp + " and " + expression.getExp());
        getParams().forEach(newExp::addParameter);
        expression.getParams().forEach(newExp::addParameter);
        return newExp;
    }
}
