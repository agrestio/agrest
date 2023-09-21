package io.agrest.exp;

import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.exp.parser.AgExpressionParser;
import io.agrest.exp.parser.AgExpressionParserVisitor;
import io.agrest.exp.parser.ExpNamedParameter;
import io.agrest.exp.parser.ExpScalar;
import io.agrest.exp.parser.ExpScalarList;
import io.agrest.exp.parser.Node;
import io.agrest.exp.parser.SimpleNode;
import io.agrest.protocol.Exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AgExpression extends SimpleNode {

    protected final static Object PRUNED_NODE = new Object();

    public AgExpression(int id) {
        super(id);
    }

    public AgExpression(AgExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }

    @Override
    public Exp positionalParams(Object... params) {
        AgExpression copy = deepCopy();
        copy.inPlaceParams(params);
        return copy;
    }

    @Override
    public Exp namedParams(Map<String, Object> params) {
        return namedParams(params, true);
    }

    @Override
    public Exp namedParams(Map<String, Object> params, boolean pruneMissing) {
        return transform(new NamedParamTransformer(params, pruneMissing));
    }

    void inPlaceParams(Object... params) {
        InPlaceParamReplacer replacer = new InPlaceParamReplacer(params == null ? new Object[0] : params);
        traverse(replacer);
        replacer.onFinish();
    }

    public AgExpression deepCopy() {
        return transform(o -> o);
    }

    protected abstract AgExpression shallowCopy();

    protected int getOperandCount() {
        return jjtGetNumChildren();
    }

    protected Object getOperand(int index) {
        Node child = jjtGetChild(index);
        return unwrapChild(child);
    }

    protected void setOperand(int index, Object value) {
        Node node = (value == null || value instanceof Node)
                ? (Node) value
                : new ExpScalar(value);
        jjtAddChild(node, index);

        if (node != null) {
            node.jjtSetParent(this);
        }
    }

    protected void traverse(TraversalHandler visitor) {
        if (visitor == null) {
            throw new NullPointerException("Null Visitor.");
        }

        traverse(null, visitor);
    }

    protected void traverse(AgExpression parentExp, TraversalHandler visitor) {

        visitor.startNode(this, parentExp);

        // recursively traverse each child
        int count = getOperandCount();
        for (int i = 0; i < count; i++) {
            Object child = getOperand(i);

            if (child instanceof AgExpression && !(child instanceof ExpScalar)) {
                AgExpression childExp = (AgExpression) child;
                childExp.traverse(this, visitor);
            } else {
                visitor.objectNode(child, this);
            }

            visitor.finishedChild(this, i, i < count - 1);
        }

        visitor.endNode(this, parentExp);
    }

    protected AgExpression transform(Function<Object, Object> transformer) {

        Object transformed = transformExpression(transformer);
        if (transformed == PRUNED_NODE || transformed == null) {
            return null;
        } else if (transformed instanceof AgExpression) {
            return (AgExpression) transformed;
        }

        throw new AgExpressionException("Invalid transformed expression: " + transformed);
    }

    protected Object transformExpression(Function<Object, Object> transformer) {
        AgExpression copy = shallowCopy();
        int count = getOperandCount();
        for (int i = 0, j = 0; i < count; i++) {
            AgExpression child = (AgExpression) jjtGetChild(i);
            Object transformedChild = child.transformExpression(transformer);

            boolean prune = transformedChild == PRUNED_NODE;

            if (!prune) {
                copy.setOperand(j, transformedChild);
                j++;
            }

            if (prune && pruneNodeForPrunedChild()) {
                // bail out early...
                return PRUNED_NODE;
            }
        }

        // all the children are processed, only now transform this copy
        return transformer.apply(copy);
    }

    protected boolean pruneNodeForPrunedChild() {
        return true;
    }

    private Object unwrapChild(Node child) {
        return (child instanceof ExpScalar) ? ((ExpScalar) child).getValue() : child;
    }

    private static Object wrapParameterValue(Object value) {
        if (value instanceof Collection<?>) {
            return new ExpScalarList((Collection<?>) value);
        } else if (value instanceof Object[]) {
            return new ExpScalarList((Object[]) value);
        } else if (value instanceof TextNode) {
            return ((TextNode) value).asText();
        } else {
            return value;
        }
    }

    final static class NamedParamTransformer implements Function<Object, Object> {

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
                if (pruneMissing) {
                    return PRUNED_NODE;
                } else {
                    throw new AgExpressionException("Missing required parameter: $" + name);
                }
            } else {
                Object value = parameters.get(name);
                return value != null ? wrapParameterValue(value) : new ExpScalar(null);
            }
        }
    }

    final static class InPlaceParamReplacer implements TraversalHandler {

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
        public void finishedChild(AgExpression node, int childIndex, boolean hasMoreChildren) {

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
            return (p != null) ? wrapParameterValue(p) : new ExpScalar(null);
        }
    }
}
