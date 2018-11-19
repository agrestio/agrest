package io.agrest.backend.exp;

import io.agrest.backend.exp.parser.ASTAdd;
import io.agrest.backend.exp.parser.ASTAnd;
import io.agrest.backend.exp.parser.ASTBetween;
import io.agrest.backend.exp.parser.ASTBitwiseAnd;
import io.agrest.backend.exp.parser.ASTBitwiseLeftShift;
import io.agrest.backend.exp.parser.ASTBitwiseNot;
import io.agrest.backend.exp.parser.ASTBitwiseOr;
import io.agrest.backend.exp.parser.ASTBitwiseRightShift;
import io.agrest.backend.exp.parser.ASTBitwiseXor;
import io.agrest.backend.exp.parser.ASTDbPath;
import io.agrest.backend.exp.parser.ASTDivide;
import io.agrest.backend.exp.parser.ASTEqual;
import io.agrest.backend.exp.parser.ASTFalse;
import io.agrest.backend.exp.parser.ASTGreater;
import io.agrest.backend.exp.parser.ASTGreaterOrEqual;
import io.agrest.backend.exp.parser.ASTIn;
import io.agrest.backend.exp.parser.ASTLess;
import io.agrest.backend.exp.parser.ASTLessOrEqual;
import io.agrest.backend.exp.parser.ASTLike;
import io.agrest.backend.exp.parser.ASTLikeIgnoreCase;
import io.agrest.backend.exp.parser.ASTMultiply;
import io.agrest.backend.exp.parser.ASTNegate;
import io.agrest.backend.exp.parser.ASTNot;
import io.agrest.backend.exp.parser.ASTNotBetween;
import io.agrest.backend.exp.parser.ASTNotEqual;
import io.agrest.backend.exp.parser.ASTNotIn;
import io.agrest.backend.exp.parser.ASTNotLike;
import io.agrest.backend.exp.parser.ASTNotLikeIgnoreCase;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.exp.parser.ASTList;
import io.agrest.backend.exp.parser.ASTOr;
import io.agrest.backend.exp.parser.ASTSubtract;
import io.agrest.backend.exp.parser.ASTTrue;
import io.agrest.backend.exp.parser.ExpressionParser;
import io.agrest.backend.exp.parser.ExpressionParserTokenManager;
import io.agrest.backend.exp.parser.JavaCharStream;
import io.agrest.backend.exp.parser.SimpleNode;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Collection;


public class ExpressionFactory {

    private static final int PARSE_BUFFER_MAX_SIZE = 4096;

    private static Constructor<? extends Expression>[] typeLookup;

    static {
        // make sure all types are small integers, then we can use them as indexes in lookup array
        int[] allTypes = new int[] { Expression.AND, Expression.OR, Expression.NOT, Expression.EQUAL_TO,
                Expression.NOT_EQUAL_TO, Expression.LESS_THAN, Expression.GREATER_THAN, Expression.LESS_THAN_EQUAL_TO,
                Expression.GREATER_THAN_EQUAL_TO, Expression.BETWEEN, Expression.IN, Expression.LIKE,
                Expression.LIKE_IGNORE_CASE, Expression.ADD, Expression.SUBTRACT, Expression.MULTIPLY,
                Expression.DIVIDE, Expression.NEGATIVE, Expression.OBJ_PATH, Expression.DB_PATH, Expression.LIST,
                Expression.NOT_BETWEEN, Expression.NOT_IN, Expression.NOT_LIKE, Expression.NOT_LIKE_IGNORE_CASE,
                Expression.TRUE, Expression.FALSE, Expression.BITWISE_NOT, Expression.BITWISE_AND,
                Expression.BITWISE_OR, Expression.BITWISE_XOR, Expression.BITWISE_LEFT_SHIFT,
                Expression.BITWISE_RIGHT_SHIFT };

        int max = 0;
        for (int type : allTypes) {
            // sanity check....
            if (type > 500) {
                throw new RuntimeException("Types values are too big: " + type);
            } else if (type < 0) {
                throw new RuntimeException("Types values are too small: " + type);
            }
            if (type > max) {
                max = type;
            }
        }

        // now we know that if types are used as indexes,
        // they will fit in array "max + 1" long (though gaps are possible)
        @SuppressWarnings("unchecked")
        Constructor<? extends SimpleNode>[] lookupTable = (Constructor<? extends SimpleNode>[]) new Constructor[max + 1];
        typeLookup = lookupTable;

        try {
            typeLookup[Expression.AND] = ASTAnd.class.getDeclaredConstructor();
            typeLookup[Expression.OR] = ASTOr.class.getDeclaredConstructor();
            typeLookup[Expression.BETWEEN] = ASTBetween.class.getDeclaredConstructor();
            typeLookup[Expression.NOT_BETWEEN] = ASTNotBetween.class.getDeclaredConstructor();

            // binary types
            typeLookup[Expression.EQUAL_TO] = ASTEqual.class.getDeclaredConstructor();
            typeLookup[Expression.NOT_EQUAL_TO] = ASTNotEqual.class.getDeclaredConstructor();
            typeLookup[Expression.LESS_THAN] = ASTLess.class.getDeclaredConstructor();
            typeLookup[Expression.GREATER_THAN] = ASTGreater.class.getDeclaredConstructor();
            typeLookup[Expression.LESS_THAN_EQUAL_TO] = ASTLessOrEqual.class.getDeclaredConstructor();
            typeLookup[Expression.GREATER_THAN_EQUAL_TO] = ASTGreaterOrEqual.class.getDeclaredConstructor();
            typeLookup[Expression.IN] = ASTIn.class.getDeclaredConstructor();
            typeLookup[Expression.NOT_IN] = ASTNotIn.class.getDeclaredConstructor();
            typeLookup[Expression.LIKE] = ASTLike.class.getDeclaredConstructor();
            typeLookup[Expression.LIKE_IGNORE_CASE] = ASTLikeIgnoreCase.class.getDeclaredConstructor();
            typeLookup[Expression.NOT_LIKE] = ASTNotLike.class.getDeclaredConstructor();
            typeLookup[Expression.NOT_LIKE_IGNORE_CASE] = ASTNotLikeIgnoreCase.class.getDeclaredConstructor();
            typeLookup[Expression.ADD] = ASTAdd.class.getDeclaredConstructor();
            typeLookup[Expression.SUBTRACT] = ASTSubtract.class.getDeclaredConstructor();
            typeLookup[Expression.MULTIPLY] = ASTMultiply.class.getDeclaredConstructor();
            typeLookup[Expression.DIVIDE] = ASTDivide.class.getDeclaredConstructor();

            typeLookup[Expression.NOT] = ASTNot.class.getDeclaredConstructor();
            typeLookup[Expression.NEGATIVE] = ASTNegate.class.getDeclaredConstructor();
            typeLookup[Expression.OBJ_PATH] = ASTObjPath.class.getDeclaredConstructor();
            typeLookup[Expression.DB_PATH] = ASTDbPath.class.getDeclaredConstructor();
            typeLookup[Expression.LIST] = ASTList.class.getDeclaredConstructor();

            typeLookup[Expression.TRUE] = ASTTrue.class.getDeclaredConstructor();
            typeLookup[Expression.FALSE] = ASTFalse.class.getDeclaredConstructor();

            typeLookup[Expression.BITWISE_NOT] = ASTBitwiseNot.class.getDeclaredConstructor();
            typeLookup[Expression.BITWISE_OR] = ASTBitwiseOr.class.getDeclaredConstructor();
            typeLookup[Expression.BITWISE_AND] = ASTBitwiseAnd.class.getDeclaredConstructor();
            typeLookup[Expression.BITWISE_XOR] = ASTBitwiseXor.class.getDeclaredConstructor();
            typeLookup[Expression.BITWISE_LEFT_SHIFT] = ASTBitwiseLeftShift.class.getDeclaredConstructor();
            typeLookup[Expression.BITWISE_RIGHT_SHIFT] = ASTBitwiseRightShift.class.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new ExpressionException("Wrong expression type found", ex);
        }
    }


    /**
     * A convenience method to create an DB_PATH "equal to" expression.
     */
    public static Expression matchDbExp(String pathSpec, Object value) {
        return new ASTEqual(new ASTDbPath(pathSpec), value);
    }

    public static Expression and(Collection<Expression> expressions) {
        return joinExp(Expression.AND, expressions);
    }

    /**
     * Parses string, converting it to Expression and optionally binding
     * positional parameters. If a string does not represent a semantically
     * correct expression, an ExpressionException is thrown.
     * <p>
     * Binding of parameters by name (as opposed to binding by position) can be
     * achieved by chaining this call with { Expression#params(Map)}.
     */
    public static Expression exp(String expressionString, Object... parameters) {
        Expression e = fromString(expressionString);

        if (parameters != null && parameters.length > 0) {
            // apply parameters in-place... it is wasteful to clone the
            // expression that hasn't been exposed to the callers
            e.inPlaceParamsArray(parameters);
        }

        return e;
    }

    /**
     * Joins all expressions, making a single expression. <code>type</code> is
     * used as an expression type for expressions joining each one of the items
     * on the list. <code>type</code> must be binary expression type.
     * <p>
     * For example, if type is Expression.AND, resulting expression would match
     * all expressions in the list. If type is Expression.OR, resulting
     * expression would match any of the expressions.
     * </p>
     */
    public static Expression joinExp(int type, Collection<Expression> expressions) {
        int len = expressions.size();
        if (len == 0) {
            return null;
        }

        return joinExp(type, expressions.toArray(new Expression[len]));
    }

    /**
     * Joins all expressions, making a single expression. <code>type</code> is
     * used as an expression type for expressions joining each one of the items
     * in the array. <code>type</code> must be binary expression type.
     * <p>
     * For example, if type is Expression.AND, resulting expression would match
     * all expressions in the list. If type is Expression.OR, resulting
     * expression would match any of the expressions.
     * </p>
     */
    public static Expression joinExp(int type, Expression... expressions) {

        int len = expressions != null ? expressions.length : 0;
        if (len == 0) {
            return null;
        }

        Expression currentExp = expressions[0];
        if (len == 1) {
            return currentExp;
        }

        Expression exp = expressionOfType(type);
        for (int i = 0; i < len; i++) {
            exp.setOperand(i, expressions[i]);
        }
        return exp;
    }

    /**
     * Creates a new expression for the type requested. If type is unknown,
     * ExpressionException is thrown.
     */
    public static Expression expressionOfType(int type) {
        if (type < 0 || type >= typeLookup.length) {
            throw new RuntimeException("Bad expression type: " + type);
        }

        if (typeLookup[type] == null) {
            throw new RuntimeException("Bad expression type: " + type);
        }

        // expected this
        try {
            return typeLookup[type].newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Error creating expression", ex);
        }
    }

    /**
     * A convenience method to create an OBJ_PATH "equal to" expression.
     */
    public static Expression matchExp(String pathSpec, Object value) {
        return matchExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * A convenience shortcut for boolean true expression.
     */
    public static Expression expTrue() {
        return new ASTTrue();
    }

    /**
     * A convenience shortcut for boolean false expression.
     */
    public static Expression expFalse() {
        return new ASTFalse();
    }

    /**
     * A convenience method to create an OBJ_PATH "greater than" expression.
     */
    public static Expression greaterExp(String pathSpec, Object value) {
        return greaterExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * @see ExpressionFactory#greaterExp(String, Object)
     */
    static Expression greaterExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTGreater((SimpleNode)exp, value);
    }

    /**
     * @see ExpressionFactory#matchExp(String, Object)
     */
    static Expression matchExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTEqual((SimpleNode)exp, value);
    }

    /**
     * A convenience method to create an OBJ_PATH "greater than or equal to"
     * expression.
     */
    public static Expression greaterOrEqualExp(String pathSpec, Object value) {
        return greaterOrEqualExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * @see ExpressionFactory#greaterOrEqualExp(String, Object)
     */
    static Expression greaterOrEqualExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTGreaterOrEqual((SimpleNode)exp, value);
    }

    /**
     * A convenience shortcut for building IN expression. Return ASTFalse for
     * empty collection.
     */
    public static Expression inExp(String pathSpec, Object... values) {
        return inExp(new ASTObjPath(pathSpec), values);
    }

    /**
     * @see ExpressionFactory#inExp(String, Object[])
     */
    static Expression inExp(Expression exp, Object... values) {
        if (values.length == 0) {
            return new ASTFalse();
        }
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTIn((SimpleNode)exp, new ASTList(values));
    }

    /**
     * A convenience shortcut for building IN expression. Return ASTFalse for
     * empty collection.
     */
    public static Expression inExp(String pathSpec, Collection<?> values) {
        return inExp(new ASTObjPath(pathSpec), values);
    }

    /**
     * @see ExpressionFactory#inExp(String, Collection)
     */
    static Expression inExp(Expression exp, Collection<?> values) {
        if (values.isEmpty()) {
            return new ASTFalse();
        }
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTIn((SimpleNode)exp, new ASTList(values));
    }

    /**
     * A convenience method to create an OBJ_PATH "less than" expression.
     */
    public static Expression lessExp(String pathSpec, Object value) {
        return lessExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * @see ExpressionFactory#lessExp(String, Object)
     */
    static Expression lessExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTLess((SimpleNode)exp, value);
    }

    /**
     * A convenience method to create an OBJ_PATH "less than or equal to"
     * expression.
     */
    public static Expression lessOrEqualExp(String pathSpec, Object value) {
        return lessOrEqualExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * @see ExpressionFactory#lessOrEqualExp(String, Object)
     */
    static Expression lessOrEqualExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTLessOrEqual((SimpleNode)exp, value);
    }

    /**
     * A convenience shortcut for building LIKE_IGNORE_CASE expression.
     */
    public static Expression likeIgnoreCaseExp(String pathSpec, Object value) {
        return likeIgnoreCaseExpInternal(pathSpec, value, (char) 0);
    }

    static ASTLikeIgnoreCase likeIgnoreCaseExpInternal(String pathSpec, Object value, char escapeChar) {
        return likeIgnoreCaseExp(new ASTObjPath(pathSpec), value, escapeChar);
    }

    static ASTLikeIgnoreCase likeIgnoreCaseExp(Expression exp, Object value, char escapeChar) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTLikeIgnoreCase((SimpleNode) exp, value, escapeChar);
    }

    /**
     * A convenience method to create an OBJ_PATH "not equal to" expression.
     */
    public static Expression noMatchExp(String pathSpec, Object value) {
        return noMatchExp(new ASTObjPath(pathSpec), value);
    }

    /**
     * @see ExpressionFactory#noMatchExp(String, Object)
     */
    static Expression noMatchExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTNotEqual((SimpleNode)exp, value);
    }

    /**
     * Applies a few default rules for adding operands to expressions. In
     * particular wraps all lists into LIST expressions. Applied only in path
     * expressions.
     */
    protected static Object wrapPathOperand(Object op) {
        if (op instanceof Collection<?>) {
            return new ASTList((Collection<?>) op);
        } else if (op instanceof Object[]) {
            return new ASTList((Object[]) op);
        } else {
            return op;
        }
    }

    /**
     * Parses string, converting it to Expression. If string does not represent
     * a semantically correct expression, an ExpressionException is thrown.
     */
    private static Expression fromString(String expressionString) {

        if (expressionString == null) {
            throw new NullPointerException("Null expression string.");
        }

        // optimizing parser buffers per CAY-1667...
        // adding 1 extra char to the buffer size above the String length, as
        // otherwise resizing still occurs at the end of the stream
        int bufferSize = expressionString.length() > PARSE_BUFFER_MAX_SIZE ?
                PARSE_BUFFER_MAX_SIZE : expressionString.length() + 1;
        Reader reader = new StringReader(expressionString);

        JavaCharStream stream = new JavaCharStream(reader, 1, 1, bufferSize);
        ExpressionParserTokenManager tm = new ExpressionParserTokenManager(stream);
        ExpressionParser parser = new ExpressionParser(tm);

        try {
            return parser.expression();
        } catch (Throwable th) {
            String message = th.getMessage();
            throw new ExpressionException("%s", th, message != null ? message : "");
        }
    }
}
