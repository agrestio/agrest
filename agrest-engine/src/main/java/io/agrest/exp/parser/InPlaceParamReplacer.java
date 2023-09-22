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
        if (child instanceof ExpNamedParameter) {
            node.setOperand(childIndex, nextValue(((ExpNamedParameter) child).getName()));
        } else if (child instanceof Object[]) {
            Object[] array = (Object[]) child;

            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof ExpNamedParameter) {
                    array[i] = nextValue(((ExpNamedParameter) array[i]).getName());
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
