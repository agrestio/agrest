package io.agrest.client.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Expression {

    public enum ParamsType {
        NO_PARAMS, POSITIONAL, NAMED
    }

    public static ExpressionBuilder query(String query) {
        return new ExpressionBuilder(query);
    }

    private String query;
    private List<Object> paramsList;
    private Map<String, Object> paramsMap;

    private Expression(String query) {
        this.query = Objects.requireNonNull(query);
    }

    public String getQuery() {
        return query;
    }

    public List<Object> getParams() {
        return paramsList;
    }

    private void addParams(Object... params) {

        if (paramsList == null) {
            paramsList = new ArrayList<>();
        }
        Collections.addAll(this.paramsList, params);
        this.paramsMap = null;
    }

    public Map<String, Object> getParamsMap() {
        return paramsMap;
    }

    private void addParam(String name, Object value) {
        if (paramsMap == null) {
            paramsMap = new HashMap<>();
        }
        paramsMap.put(name, value);
        this.paramsList = null;
    }

    public ParamsType getParamsType() {

        if (paramsList != null) {
            return ParamsType.POSITIONAL;
        } else if (paramsMap != null) {
            return ParamsType.NAMED;
        } else {
            return ParamsType.NO_PARAMS;
        }
    }

    public static class ExpressionBuilder {

        private Expression expression;

        private ExpressionBuilder(String query) {
            expression = new Expression(query);
        }

        public ExpressionBuilder params(Object... params) {
            expression.addParams(params);
            return this;
        }

        public ExpressionBuilder param(String name, Object value) {
            expression.addParam(name, value);
            return this;
        }

        public Expression build() {
            return expression;
        }
    }
}
