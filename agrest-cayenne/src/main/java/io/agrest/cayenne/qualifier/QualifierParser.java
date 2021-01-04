package io.agrest.cayenne.qualifier;

import io.agrest.base.protocol.CayenneExp;
import io.agrest.base.protocol.exp.*;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.3
 */
public class QualifierParser implements IQualifierParser {

    @Override
    public Expression parse(CayenneExp qualifier) {

        if (qualifier == null) {
            return null;
        }

        List<Expression> stack = new ArrayList<>();

        qualifier.visit(new ExpVisitor() {

            @Override
            public void visitSimpleExp(SimpleExp exp) {
                stack.add(ExpressionFactory.exp(exp.getTemplate()));
            }

            @Override
            public void visitNamedParamsExp(NamedParamsExp exp) {
                stack.add(ExpressionFactory.exp(exp.getTemplate()).params(exp.getParams()));
            }

            @Override
            public void visitPositionalParamsExp(PositionalParamsExp exp) {
                stack.add(ExpressionFactory.exp(exp.getTemplate(), exp.getParams()));
            }

            @Override
            public void visitCompositeExp(CompositeExp exp) {

                CayenneExp[] children = exp.getParts();
                Expression[] parsedChildren = new Expression[children.length];

                // here the stack would become temporarily inconsistent (contain children without the parent)
                // Suppose it is benign, as we are controlling eh walk, still worse mentioning
                for (int i = 0; i < children.length; i++) {
                    children[i].visit(this);
                    parsedChildren[i] = stack.remove(stack.size() - 1);
                }

                switch (exp.getCombineOperand()) {
                    case CompositeExp.AND:
                        stack.add(ExpressionFactory.and(parsedChildren));
                        break;
                    case CompositeExp.OR:
                        stack.add(ExpressionFactory.or(parsedChildren));
                        break;
                    default:
                        throw new IllegalStateException("Unknown combine operand: " + exp.getCombineOperand());
                }
            }
        });

        return stack.isEmpty() ? null : stack.get(0);
    }
}
