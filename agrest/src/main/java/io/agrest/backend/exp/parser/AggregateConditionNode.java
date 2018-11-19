package io.agrest.backend.exp.parser;

import io.agrest.backend.exp.ExpressionException;

import java.util.function.Function;

/**
 * Superclass of aggregated conditional nodes such as NOT, AND, OR. Performs
 * extra checks on parent and child expressions to validate conditions that are
 * not addressed in the Cayenne expressions grammar.
 */
public abstract class AggregateConditionNode extends SimpleNode {

	AggregateConditionNode(int i) {
		super(i);
	}

	@Override
	protected boolean pruneNodeForPrunedChild(Object prunedChild) {
		return false;
	}

	@Override
	protected Object transformExpression(Function<Object, Object> transformer) {
		Object transformed = super.transformExpression(transformer);

		if (!(transformed instanceof AggregateConditionNode)) {
			return transformed;
		}

		AggregateConditionNode condition = (AggregateConditionNode) transformed;

		// prune itself if the transformation resulted in
		// no children or a single child
		switch (condition.getOperandCount()) {
		case 1:
			if (condition instanceof ASTNot) {
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

	@Override
	public void jjtSetParent(Node n) {
		// this is a check that we can't handle properly
		// in the grammar... do it here...

		// disallow non-aggregated condition parents...
		if (!(n instanceof AggregateConditionNode)) {
			String label = (n instanceof SimpleNode) ? ((SimpleNode) n).expName() : String.valueOf(n);
			throw new ExpressionException(expName() + ": invalid parent - " + label);
		}

		super.jjtSetParent(n);
	}

	@Override
	public void jjtAddChild(Node n, int i) {
		// this is a check that we can't handle properly
		// in the grammar... do it here...

		// only allow conditional nodes...no scalars
		if (!(n instanceof ConditionNode) && !(n instanceof AggregateConditionNode)) {
			String label = (n instanceof SimpleNode) ? ((SimpleNode) n).expName() : String.valueOf(n);
			throw new ExpressionException(expName() + ": invalid child - " + label);
		}

		super.jjtAddChild(n, i);
	}
}
