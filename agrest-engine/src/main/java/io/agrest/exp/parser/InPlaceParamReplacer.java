package io.agrest.exp.parser;

import io.agrest.exp.AgExpressionException;
import io.agrest.exp.TraversalHandler;

import java.util.HashMap;
import java.util.Map;

class InPlaceParamReplacer implements TraversalHandler {

    private final Object[] parameters;
    private int i;
    private Map<String, Object> seen;

    InPlaceParamReplacer(Object[] parameters) {
        this.parameters = parameters;
    }

    void onFinish() {
        if (i < parameters.length) {
            throw new AgExpressionException("Too many parameters to bind expression. "
                    + "Expected: " + i + ", actual: " + parameters.length);
        }
    }

    @Override
    public void finishedChild(SimpleNode node, int childIndex, boolean hasMoreChildren) {

        Object child = node.getOperand(childIndex);
        if (child instanceof ExpNamedParameter param) {
            node.setOperand(childIndex, nextValue(param.getName()));
        } else if (child instanceof Object[] array) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof ExpNamedParameter param) {
                    array[i] = nextValue(param.getName());
                }
            }
        }
    }

    private Object nextValue(String name) {

        if (seen == null) {
            seen = new HashMap<>();
        }

        Object p;
        if (seen.containsKey(name)) {
            p = seen.get(name);
        } else {
            if (i >= parameters.length) {
                throw new AgExpressionException("Too few parameters to bind expression: " + parameters.length);
            }

            p = parameters[i++];
            seen.put(name, p);
        }
        return (p != null) ? SimpleNode.wrapParameterValue(p) : new ExpScalar(null);
    }
}
