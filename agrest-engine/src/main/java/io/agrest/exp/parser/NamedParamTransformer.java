package io.agrest.exp.parser;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

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
            // after parameters are resolved, we may need to shake down the tree a bit
            return optimize(object);
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

    private Object optimize(Object object) {
        if(object instanceof SimpleNode) {
            return ((SimpleNode) object).jjtAccept(new OptimizationVisitor(), null);
        }
        return object;
    }

    static class OptimizationVisitor extends AgExpressionParserDefaultVisitor<SimpleNode> {

        @Override
        public SimpleNode defaultVisit(SimpleNode node, SimpleNode data) {
            // note, we do not go down to children, just process this node and that's it
            return node;
        }

        @Override
        public SimpleNode visit(ExpIn node, SimpleNode data) {
            return optimizeIn(node, ExpFalse::new);
        }

        @Override
        public SimpleNode visit(ExpNotIn node, SimpleNode data) {
            return optimizeIn(node, ExpTrue::new);
        }

        private static SimpleNode optimizeIn(SimpleNode node, Supplier<SimpleNode> supplier) {
            if(node.jjtGetNumChildren() < 2) {
                return node;
            }
            Node child = node.jjtGetChild(1);
            if(child instanceof ExpScalarList) {
                if(((ExpScalarList) child).getValue().isEmpty()) {
                    return supplier.get();
                }
            }
            return node;
        }
    }
}
