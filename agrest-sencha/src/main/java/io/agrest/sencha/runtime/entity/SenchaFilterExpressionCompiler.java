package io.agrest.sencha.runtime.entity;

import io.agrest.AgException;
import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgEntity;
import io.agrest.sencha.ops.FilterUtil;
import io.agrest.sencha.protocol.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SenchaFilterExpressionCompiler implements ISenchaFilterExpressionCompiler {

    private static final int MAX_VALUE_LENGTH = 1024;

    @Override
    public List<Exp> process(AgEntity<?> entity, List<Filter> filters) {

        if (filters.isEmpty()) {
            return Collections.emptyList();
        }

        List<Exp> exps = new ArrayList<>(filters.size());
        filters.stream().filter(f -> !f.isDisabled()).forEach(f -> exps.add(fromFilter(f)));
        return exps;
    }

    Exp fromFilter(Filter filter) {
        switch (filter.getOperator()) {
            case "=":
            case ">":
            case ">=":
            case "<":
            case "<=":
                return fromFilter(filter, filter.getOperator());
            case "!=":
                return fromFilter(filter, "<>");
            case "like":
                return like(filter);
            case "in":
                return in(filter);
            default:
                throw AgException.badRequest("Invalid filter operator: %s", filter.getOperator());
        }
    }

    Exp like(Filter filter) {

        if (filter.getValue() == null || filter.isExactMatch() || filter.getValue() instanceof Boolean) {
            return fromFilter(filter, "=");
        }

        String string = filter.getValue().toString();
        checkValueLength(string);
        return Exp.simple(filter.getProperty() + " likeIgnoreCase '" + FilterUtil.escapeValueForLike(string) + "%'");
    }


    Exp in(Filter filter) {

        if (!(filter.getValue() instanceof List)) {
            return fromFilter(filter, "=");
        }

        return Exp.withPositionalParams(filter.getProperty() + " in ($a)", filter.getValue());
    }

    Exp fromFilter(Filter filter, String op) {
        return Exp.withPositionalParams(filter.getProperty() + " " + op + " $a", filter.getValue());
    }

    private void checkValueLength(String value) {
        if (value.length() > MAX_VALUE_LENGTH) {
            throw AgException.badRequest("filter 'value' is to long: %s", value);
        }
    }
}
