package io.agrest.backend.exp;

import io.agrest.backend.util.ConversionUtil;
import org.apache.cayenne.exp.ExpressionException;
import org.apache.cayenne.exp.ExpressionParameter;
import org.apache.cayenne.exp.parser.ASTScalar;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author vyarmolovich
 * 10/24/18
 */
public abstract class Expression {

    /**
     * A value that a Transformer might return to indicate that a node has to be
     * pruned from the expression during the transformation.
     */
    public final static Object PRUNED_NODE = new Object();

    public static final int AND = 0;
    public static final int OR = 1;
//    public static final int NOT = 2;
//    public static final int EQUAL_TO = 3;
//    public static final int NOT_EQUAL_TO = 4;
//    public static final int LESS_THAN = 5;
//    public static final int GREATER_THAN = 6;
//    public static final int LESS_THAN_EQUAL_TO = 7;
//    public static final int GREATER_THAN_EQUAL_TO = 8;
//    public static final int BETWEEN = 9;
//    public static final int IN = 10;
//    public static final int LIKE = 11;
//    public static final int LIKE_IGNORE_CASE = 12;
//    public static final int ADD = 16;
//    public static final int SUBTRACT = 17;
//    public static final int MULTIPLY = 18;
//    public static final int DIVIDE = 19;
//    public static final int NEGATIVE = 20;
//    public static final int TRUE = 21;
//    public static final int FALSE = 22;


    /**
     * Returns a count of operands of this expression. In real life there are
     * unary (count == 1), binary (count == 2) and ternary (count == 3)
     * expressions.
     */
    public abstract int getOperandCount();

    /**
     * Returns a value of operand at <code>index</code>. Operand indexing starts
     * at 0.
     */
    public abstract Object getOperand(int index);

    /**
     * Sets a value of operand at <code>index</code>. Operand indexing starts at
     * 0.
     */
    public abstract void setOperand(int index, Object value);

    /**
     * Restructures expression to make sure that there are no children of the
     * same type as this expression.
     *
     */
    protected abstract void flattenTree();

//    /**
//     * Calculates expression value with object as a context for path
//     * expressions.
//     *
//     */
//    public abstract Object evaluate(Object o);
//
//    /**
//     * Appends own content as a String to the provided Appendable.
//     *
//     * @throws IOException
//     */
//    public abstract void appendAsString(Appendable out) throws IOException;

    /**
     * Creates a copy of this expression node, without copying children.
     */
    public abstract Expression shallowCopy();

    /**
     * Returns true if this node should be pruned from expression tree in the
     * event a child is removed.
     */
    protected abstract boolean pruneNodeForPrunedChild(Object prunedChild);


    /**
     * Chains this expression with another expression using "and".
     */
    public Expression andExp(Expression exp) {
        return joinExp(Expression.AND, exp);
    }

    /**
     * Chains this expression with another expression using "or".
     */
    public Expression orExp(Expression exp) {
        return joinExp(Expression.OR, exp);
    }

    /**
     * Calculates expression boolean value with object as a context for path
     * expressions.
     *
     */
    public boolean match(Object o) {
//        return ConversionUtil.toBoolean(evaluate(o));
        return false;
    }

    /**
     * Creates a new expression that joins this object with another expression,
     * using specified join type. It is very useful for incrementally building
     * chained expressions, like long AND or OR statements.
     */
    public Expression joinExp(int type, Expression exp) {
        return joinExp(type, exp, new Expression[0]);
    }

    /**
     * Creates a new expression that joins this object with other expressions,
     * using specified join type. It is very useful for incrementally building
     * chained expressions, like long AND or OR statements.
     */
    public Expression joinExp(int type, Expression exp, Expression... expressions) {
        Expression join = ExpressionFactory.expressionOfType(type);
        join.setOperand(0, this);
        join.setOperand(1, exp);
        for (int i = 0; i < expressions.length; i++) {
            Expression expressionInArray = expressions[i];
            join.setOperand(2 + i, expressionInArray);
        }
        join.flattenTree();
        return join;
    }

    /**
     * Creates and returns a new Expression instance based on this expression,
     * but with named parameters substituted with provided values. Any
     * subexpressions containing parameters not matching the "name" argument
     * will be pruned.
     * <p>
     * Note that if you want matching against nulls to be preserved, you must
     * place NULL values for the corresponding keys in the map.
     *
     * @since 4.0
     */
    public Expression params(Map<String, ?> parameters) {
        return transform(new NamedParamTransformer(parameters, true));
    }

    /**
     * Creates and returns a new Expression instance based on this expression,
     * but with named parameters substituted with provided values.If any
     * subexpressions containing parameters not matching the "name" argument are
     * found, the behavior depends on "pruneMissing" argument. If it is false an
     * Exception will be thrown, otherwise subexpressions with missing
     * parameters will be pruned from the resulting expression.
     * <p>
     * Note that if you want matching against nulls to be preserved, you must
     * place NULL values for the corresponding keys in the map.
     *
     * @since 4.0
     */
    public Expression params(Map<String, ?> parameters, boolean pruneMissing) {
        return transform(new NamedParamTransformer(parameters, pruneMissing));
    }

//    @Override
//    public String toString() {
//        StringBuilder out = new StringBuilder();
//        try {
//            appendAsString(out);
//        } catch (IOException e) {
//            throw new RuntimeException("Unexpected IO exception appending to StringBuilder", e);
//        }
//        return out.toString();
//    }


    /**
     * Creates a transformed copy of this expression, applying transformation
     * provided by Transformer to all its nodes. Null transformer will result in
     * an identical deep copy of this expression.
     * <p>
     * To force a node and its children to be pruned from the copy, Transformer
     * should return Expression.PRUNED_NODE. Otherwise an expectation is that if
     * a node is an Expression it must be transformed to null or another
     * Expression. Any other object type would result in a ExpressionException.
     *
     * @since 1.1
     */
    public Expression transform(Function<Object, Object> transformer) {
        Object transformed = transformExpression(transformer);

        if (transformed == PRUNED_NODE || transformed == null) {
            return null;
        } else if (transformed instanceof Expression) {
            return (Expression) transformed;
        }

        throw new ExpressionException("Invalid transformed expression: " + transformed);
    }

    /**
     * A recursive method called from "transform" to do the actual
     * transformation.
     *
     * @return null, Expression.PRUNED_NODE or transformed expression.
     * @since 1.2
     */
    protected Object transformExpression(Function<Object, Object> transformer) {
        Expression copy = shallowCopy();
        int count = getOperandCount();
        for (int i = 0, j = 0; i < count; i++) {
            Object operand = getOperand(i);
            Object transformedChild;

            if (operand instanceof Expression) {
                transformedChild = ((Expression) operand).transformExpression(transformer);
            } else if (transformer != null) {
                transformedChild = transformer.apply(operand);
            } else {
                transformedChild = operand;
            }

            // prune null children only if there is a transformer and it
            // indicated so
            boolean prune = transformer != null && transformedChild == PRUNED_NODE;

            if (!prune) {
                copy.setOperand(j, transformedChild);
                j++;
            }

            if (prune && pruneNodeForPrunedChild(operand)) {
                // bail out early...
                return PRUNED_NODE;
            }
        }

        // all the children are processed, only now transform this copy
        return (transformer != null) ? transformer.apply(copy) : copy;
    }


    final class NamedParamTransformer implements Function<Object, Object> {

        private Map<String, ?> parameters;
        private boolean pruneMissing;

        NamedParamTransformer(Map<String, ?> parameters, boolean pruneMissing) {
            this.parameters = parameters;
            this.pruneMissing = pruneMissing;
        }

        @Override
        public Object apply(Object object) {
            if (!(object instanceof ExpressionParameter)) {

                // normally Object[] is an ASTList child
                if (object instanceof Object[]) {

                    Object[] source = (Object[]) object;
                    int len = source.length;
                    Object[] target = new Object[len];

                    for (int i = 0; i < len; i++) {
                        target[i] = apply(source[i]);
                    }

                    return target;
                }

                return object;
            }

            String name = ((ExpressionParameter) object).getName();
            if (!parameters.containsKey(name)) {
                if (pruneMissing) {
                    return PRUNED_NODE;
                } else {
                    throw new ExpressionException("Missing required parameter: $" + name);
                }
            } else {
                Object value = parameters.get(name);

                // wrap lists (for now); also support null parameters
                // TODO: andrus 8/14/2007 - shouldn't we also wrap non-null
                // object
                // values in ASTScalars?
                return (value != null) ? ExpressionFactory.wrapPathOperand(value) : new ASTScalar(null);
            }
        }

    }
}
