package io.agrest.exp.parser;

import java.util.Map;
import java.util.function.Function;

class NamedParamTransformer implements Function<Object, Object> {

    private final Map<String, ?> parameters;
    private final boolean pruneMissing;

    NamedParamTransformer(Map<String, ?> parameters, boolean pruneMissing) {
        this.parameters = parameters;
        this.pruneMissing = pruneMissing;
    }

    @Override
    public Object apply(Object object) {

        if (!(object instanceof ExpNamedParameter)) {
            return object;
        }

        String name = ((ExpNamedParameter) object).getName();
        if (!parameters.containsKey(name)) {

            // allow partial parameter resolution. It may be quiet useful
            return pruneMissing ? SimpleNode.PRUNED_NODE : object;

        } else {
            Object value = parameters.get(name);
            return value != null ? SimpleNode.wrapParameterValue(value) : new ExpScalar(null);
        }
    }
}
