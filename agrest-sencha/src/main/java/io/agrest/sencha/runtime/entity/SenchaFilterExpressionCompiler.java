package io.agrest.sencha.runtime.entity;

import io.agrest.AgException;
import io.agrest.base.protocol.CayenneExp;
import io.agrest.meta.AgEntity;
import io.agrest.sencha.ops.FilterUtil;
import io.agrest.sencha.protocol.Filter;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SenchaFilterExpressionCompiler implements ISenchaFilterExpressionCompiler {

    private static final int MAX_VALUE_LENGTH = 1024;

    @Override
    public List<CayenneExp> process(AgEntity<?> entity, List<Filter> filters) {

        if (filters.isEmpty()) {
            return Collections.emptyList();
        }

        List<CayenneExp> exps = new ArrayList<>(filters.size());
        filters.stream().filter(f -> !f.isDisabled()).forEach(f -> exps.add(fromFilter(f)));
        return exps;
    }

    CayenneExp fromFilter(Filter filter) {
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
                throw new AgException(Status.BAD_REQUEST, "Invalid filter operator: " + filter.getOperator());
        }
    }

    CayenneExp like(Filter filter) {

        if (filter.getValue() == null || filter.isExactMatch() || filter.getValue() instanceof Boolean) {
            return fromFilter(filter, "=");
        }

        String string = filter.getValue().toString();
        checkValueLength(string);
        return new CayenneExp(filter.getProperty() + " likeIgnoreCase '" + FilterUtil.escapeValueForLike(string) + "%'");
    }


    CayenneExp in(Filter filter) {

        if (!(filter.getValue() instanceof List)) {
            return fromFilter(filter, "=");
        }

        return new CayenneExp(filter.getProperty() + " in ($a)", filter.getValue());
    }

    CayenneExp fromFilter(Filter filter, String op) {
        return new CayenneExp(filter.getProperty() + " " + op + " $a", filter.getValue());
    }

    private void checkValueLength(String value) {
        if (value.length() > MAX_VALUE_LENGTH) {
            throw new AgException(Status.BAD_REQUEST, "filter 'value' is to long: " + value);
        }
    }
}
