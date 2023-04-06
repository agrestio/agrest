package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;

import java.util.function.Function;

public abstract class ExpAggregateCondition extends AgExpression {

    public ExpAggregateCondition(int id) {
        super(id);
    }

    public ExpAggregateCondition(AgExpressionParser p, int id) {
        super(p, id);
    }

    @Override
    protected boolean pruneNodeForPrunedChild() {
        return false;
    }

    @Override
    protected Object transformExpression(Function<Object, Object> transformer) {
        Object transformed = super.transformExpression(transformer);

        if (!(transformed instanceof ExpAggregateCondition)) {
            return transformed;
        }
        ExpAggregateCondition condition = (ExpAggregateCondition) transformed;

        // Prune itself if the transformation resulted in no children or a single child.
        switch (condition.getOperandCount()) {
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
