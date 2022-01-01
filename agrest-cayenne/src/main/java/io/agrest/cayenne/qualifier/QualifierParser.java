package io.agrest.cayenne.qualifier;

import io.agrest.AgException;
import io.agrest.base.protocol.Exp;
import io.agrest.base.protocol.exp.CompositeExp;
import io.agrest.base.protocol.exp.ExpVisitor;
import io.agrest.base.protocol.exp.KeyValueExp;
import io.agrest.base.protocol.exp.NamedParamsExp;
import io.agrest.base.protocol.exp.PositionalParamsExp;
import io.agrest.base.protocol.exp.SimpleExp;
import io.agrest.cayenne.path.PathOps;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 3.3
 */
public class QualifierParser implements IQualifierParser {

    @Override
    public Expression parse(Exp qualifier) {

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
            public void visitKeyValueExp(KeyValueExp exp) {
                stack.add(parseKeyValueExpression(exp.getKey(), exp.getOp(), exp.getValue()));
            }

            @Override
            public void visitCompositeExp(CompositeExp exp) {

                Exp[] children = exp.getParts();
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

    static Expression parseKeyValueExpression(String key, String op, Object value) {

        ASTPath path = PathOps.parsePath(key);

        switch (op) {
            case "=":
                return ExpressionFactory.matchExp(path, value);
            case "<":
                return ExpressionFactory.lessExp(path, value);
            case ">":
                return ExpressionFactory.greaterExp(path, value);
            case "<=":
                return ExpressionFactory.lessOrEqualExp(path, value);
            case ">=":
                return ExpressionFactory.greaterOrEqualExp(path, value);
            case "like":
                return ExpressionFactory.likeExp(path, value);
            case "likeIgnoreCase":
                return ExpressionFactory.likeIgnoreCaseExp(path, value);
            case "in":
                return ExpressionFactory.inExp(path, inValues(value));
            default:
                throw new IllegalArgumentException("Unsupported operation in Expression: " + op);
        }
    }

    static Object[] inValues(Object value) {
        if (value == null) {
            return new Object[]{null};
        }

        if (value instanceof Collection) {
            return ((Collection) value).toArray(new Object[0]);
        }

        if (value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                // TODO: this limitation is arbitrary. Wrap primitives to objects
                throw AgException.internalServerError("Primitive array is not supported as an 'in' exp parameter");
            } else {
                return (Object[]) value;
            }
        }

        return new Object[]{value};
    }
}
