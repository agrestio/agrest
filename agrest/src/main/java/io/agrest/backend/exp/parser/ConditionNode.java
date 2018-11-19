package io.agrest.backend.exp.parser;


import io.agrest.backend.exp.ExpressionException;

/**
 * Superclass of conditional expressions.
 */
public abstract class ConditionNode extends SimpleNode {

    public ConditionNode(int i) {
        super(i);
    }

    @Override
    public void jjtSetParent(Node n) {
        // this is a check that we can't handle properly
        // in the grammar... do it here...

        // disallow non-aggregated condition parents...
        if (!(n instanceof AggregateConditionNode)) {
            String label = (n instanceof SimpleNode) ? ((SimpleNode)n).expName() : String.valueOf(n);
            throw new ExpressionException(expName() + ": invalid parent - " + label);
        }

        super.jjtSetParent(n);
    }
}
