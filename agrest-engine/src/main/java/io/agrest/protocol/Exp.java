package io.agrest.protocol;

import io.agrest.exp.ExpVisitor;
import io.agrest.exp.parser.AgExpressionParser;
import io.agrest.exp.parser.AgExpressionParserVisitor;
import io.agrest.exp.parser.ExpUtils;
import io.agrest.exp.parser.ExpObjPath;
import io.agrest.exp.parser.ExpRoot;
import io.agrest.exp.parser.ExpScalar;
import io.agrest.exp.parser.Node;

import java.util.Map;
import java.util.Objects;

/**
 * Represents {@link ControlParams#exp} protocol parameter.
 *
 * @since 4.4
 */
public interface Exp {

    static Exp simple(String template) {
        return parseTemplate(template);
    }

    static Exp withPositionalParams(String template, Object... params) {
        return parseTemplate(template).positionalParams(params);
    }

    static Exp withNamedParams(String template, Map<String, Object> params) {
        return parseTemplate(template).namedParams(params);
    }

    /**
     * @since 5.0
     */
    static Exp keyValue(String key, String op, Object value) {
        ExpObjPath path = ExpObjPath.of(Objects.requireNonNull(key));
        ExpScalar<?> scalar = ExpScalar.of(value);
        switch (op) {
            case "=":
                return ExpUtils.equal(path, scalar);
            case "<":
                return ExpUtils.less(path, scalar);
            case ">":
                return ExpUtils.greater(path, scalar);
            case "<=":
                return ExpUtils.lessOrEqual(path, scalar);
            case ">=":
                return ExpUtils.greaterOrEqual(path, scalar);
            case "like":
                return ExpUtils.like(path, scalar);
            case "likeIgnoreCase":
                return ExpUtils.likeIgnoreCase(path, scalar);
            case "in":
                return ExpUtils.in(path, scalar);
            default:
                throw new IllegalArgumentException("Unsupported operation in Expression: " + op);
        }
    }

    /**
     * Invokes a callback on the visitor corresponding to one of the known expression types. The operation is
     * non-recursive even for composite expressions. If the visitor needs to descend into expression tree, it will
     * need to implement this logic on its own.
     *
     * @deprecated since 5.0 in favor of {@link #accept(AgExpressionParserVisitor, Object)}
     */
    @Deprecated
    default void visit(ExpVisitor visitor) {
        // DO NOTHING
    }

    /**
     * Invokes a callback on the visitor corresponding to one of the known expression types. The operation is
     * non-recursive even for composite expressions. If the visitor needs to descend into expression tree, it will
     * need to implement this logic on its own.
     *
     * @param visitor to accept
     * @param data that passed down to the expression node
     * @return transformed data
     * @param <T> type of the data to pass down the expression tree
     *
     * @since 5.0
     */
    default <T> T accept(AgExpressionParserVisitor<T> visitor, T data) {
        return data;
    }

    default Exp and(Exp exp) {
        return exp != null ? ExpUtils.and((Node)this, (Node)exp) : this;
    }

    default Exp or(Exp exp) {
        return exp != null ? ExpUtils.or((Node)this, (Node)exp) : this;
    }

    private static ExpRoot parseTemplate(String template) {
        return AgExpressionParser.parse(Objects.requireNonNull(template));
    }
}
