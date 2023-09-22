package io.agrest.exp.parser;

import java.util.function.Function;

public abstract class ExpCondition extends SimpleNode {

    public ExpCondition(int id) {
        super(id);
    }

    public ExpCondition(AgExpressionParser p, int id) {
        super(p, id);
    }

    @Override
    protected boolean pruneNodeForPrunedChild() {
        return false;
    }

    @Override
    protected Object transformExpression(Function<Object, Object> transformer) {
        Object transformed = super.transformExpression(transformer);

        if (!(transformed instanceof ExpCondition)) {
            return transformed;
        }
        ExpCondition condition = (ExpCondition) transformed;

        // Prune itself if the transformation resulted in no children or a single child.
        switch (condition.jjtGetNumChildren()) {
            case 1:
                if (condition instanceof ExpNot) {
                    return condition;
                } else {
                    return condition.getOperand(0);
                }
            case 0:
                return PRUNED_NODE;
            default:
                return condition;
        }
    }
}
