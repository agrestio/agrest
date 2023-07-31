package io.agrest.protocol;

import io.agrest.exp.AgExpression;
import io.agrest.exp.ExpVisitor;
import io.agrest.exp.parser.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents {@link ControlParams#exp} protocol parameter.
 *
 * @since 4.4
 */
public interface Exp {

    /**
     * Creates a new expression, parsing the provided String.
     *
     * @since 5.0
     */
    static Exp parse(String expString) {
        return AgExpressionParser.parse(Objects.requireNonNull(expString));
    }

    /**
     * @deprecated since 5.0 in favor of {@link #parse(String)}
     */
    @Deprecated(since = "5.0")
    static Exp simple(String template) {
        return parse(template);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #parse(String)} and {@link #namedParams(Map)}
     */
    @Deprecated(since = "5.0")
    static Exp witNamedParams(String template, Map<String, Object> params) {
        return parse(template).namedParams(params);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #parse(String)} and {@link #positionalParams(Object...)}
     */
    @Deprecated(since = "5.0")
    static Exp withPositionalParams(String template, Object... params) {
        return parse(template).positionalParams(params);
    }

    /**
     * @since 5.0
     */
    static Exp path(String path) {
        ExpPath pathExp = new ExpPath();
        pathExp.jjtSetValue(Objects.requireNonNull(path));
        return pathExp;
    }

    /**
     * @since 5.0
     */
    static Exp scalar(Object value) {
        if (value == null) {
            return new ExpScalar(AgExpressionParserTreeConstants.JJTSCALAR);
        }

        ExpBaseScalar<?> scalar;
        if (value instanceof Collection) {
            scalar = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        } else if (value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                value = ExpUtils.wrapPrimitiveArray(value);
            } else {
                value = Arrays.asList((Object[]) value);
            }
            scalar = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        } else {
            scalar = new ExpScalar(AgExpressionParserTreeConstants.JJTSCALAR);
        }

        scalar.jjtSetValue(value);
        return scalar;
    }

    /**
     * @since 5.0
     */
    static Exp between(String left, Object right1, Object right2) {
        return ExpUtils.composeTernary(new ExpBetween(), path(left), scalar(right1), scalar(right2));
    }

    /**
     * @since 5.0
     */
    static Exp notBetween(String left, Object right1, Object right2) {
        return ExpUtils.composeTernary(new ExpNotBetween(), path(left), scalar(right1), scalar(right2));
    }

    /**
     * @since 5.0
     */
    static Exp in(String path, Object... values) {
        return ExpUtils.composeBinary(new ExpIn(), path(path), ExpUtils.scalarArray(values));
    }

    /**
     * @since 5.0
     */
    static Exp notIn(String path, Object... scalars) {
        return ExpUtils.composeBinary(new ExpNotIn(), path(path), ExpUtils.scalarArray(scalars));
    }

    /**
     * @since 5.0
     */
    static Exp inCollection(String path, Collection<?> scalars) {
        return ExpUtils.composeBinary(new ExpIn(), path(path), ExpUtils.scalarArray(scalars));
    }

    /**
     * @since 5.0
     */
    static Exp notInCollection(String path, Collection<?> scalars) {
        return ExpUtils.composeBinary(new ExpNotIn(), path(path), ExpUtils.scalarArray(scalars));
    }

    /**
     * @since 5.0
     */
    static Exp likeIgnoreCase(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpLikeIgnoreCase(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp notLikeIgnoreCase(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpNotLikeIgnoreCase(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp like(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpLike(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp notLike(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpNotLike(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp greaterOrEqual(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpGreaterOrEqual(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp lessOrEqual(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpLessOrEqual(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp greater(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpGreater(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp less(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpLess(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp equal(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpEqual(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp notEqual(String path, Object scalar) {
        return ExpUtils.composeBinary(new ExpNotEqual(), path(path), scalar(scalar));
    }

    /**
     * @since 5.0
     */
    static Exp and(Exp... exps) {
        int len = exps.length;
        switch (len) {
            case 0:
                return null;
            case 1:
                return exps[0];
            default:
                List<Node> children = new ArrayList<>(len);

                for (Exp e : exps) {
                    ExpUtils.appendAndChild(children, ((AgExpression) e).deepCopy());
                }

                ExpAnd exp = new ExpAnd();
                exp.setChildren(children.toArray(new Node[0]));

                return exp;
        }
    }

    /**
     * @since 5.0
     */
    static Exp or(Exp... exps) {
        int len = exps.length;
        switch (len) {
            case 0:
                return null;
            case 1:
                return exps[0];
            default:
                List<Node> children = new ArrayList<>(len);

                for (Exp e : exps) {
                    ExpUtils.appendOrChild(children, ((AgExpression) e).deepCopy());
                }

                ExpOr exp = new ExpOr();
                exp.setChildren(children.toArray(new Node[0]));
                return exp;
        }
    }

    /**
     * @since 5.0
     */
    static Exp not(Exp exp) {
        // delegating to the object allows polymorphic implementation
        return exp.not();
    }

    /**
     * @since 5.0
     */
    default Exp positionalParams(Object... params) {
        return this;
    }

    /**
     * @since 5.0
     */
    default Exp namedParams(Map<String, Object> params) {
        return this;
    }

    /**
     * @since 5.0
     */
    default Exp namedParams(Map<String, Object> params, boolean pruneMissing) {
        return this;
    }

    /**
     * Invokes a callback on the visitor corresponding to one of the known expression types. The operation is
     * non-recursive even for composite expressions. If the visitor needs to descend into expression tree, it will
     * need to implement this logic on its own.
     *
     * @deprecated since 5.0 in favor of {@link #accept(AgExpressionParserVisitor, Object)}
     */
    @Deprecated(since = "5.0")
    default void visit(ExpVisitor visitor) {
        // DO NOTHING
    }

    /**
     * Invokes a callback on the visitor corresponding to one of the known expression types. The operation is
     * non-recursive even for composite expressions. If the visitor needs to descend into expression tree, it will
     * need to implement this logic on its own.
     *
     * @param visitor to accept
     * @param data    that passed down to the expression node
     * @param <T>     type of the data to pass down the expression tree
     * @return transformed data
     * @since 5.0
     */
    default <T> T accept(AgExpressionParserVisitor<T> visitor, T data) {
        return data;
    }

    default Exp and(Exp exp) {
        return exp != null ? Exp.and(this, exp) : this;
    }

    default Exp or(Exp exp) {
        return exp != null ? Exp.or(this, exp) : this;
    }

    /**
     * @since 5.0
     */
    default Exp not() {
        ExpNot not = new ExpNot();
        not.jjtAddChild((Node) this, 0);
        return not;
    }
}
