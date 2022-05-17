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

    public String toJpaString() {
        return exp;
    }

    public boolean isEmpty() {
        return exp == null || exp.length() == 0;
    }

    public JpaExpression and(JpaExpression expression) {
        return join(expression, "and");
    }

    public JpaExpression or(JpaExpression expression) {
        return join(expression, "or");
    }

    private JpaExpression join(JpaExpression expression, String operator) {
        if(expression.isEmpty()) {
            return this;
        }

        String expString = reindexParams(expression.toJpaString(), expression.getParams().size());
        JpaExpression newExp = new JpaExpression(exp + " " + operator + " " + expString);
        getParams().forEach(newExp::addParameter);
        expression.getParams().forEach(newExp::addParameter);
        return newExp;
    }

    private String reindexParams(String exp, int size) {
        if(size == 0) {
            return exp;
        }
        for(int i=0; i<size; i++) {
            exp = exp.replace("?"+i, "?" + (i + params.size()));
        }
        return exp;
    }
}
