package io.agrest.backend.exp;

import io.agrest.backend.exp.parser.ASTDbPath;
import io.agrest.backend.exp.parser.ASTEqual;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.exp.parser.ASTList;
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
     *
     * @since 4.0
     */
    public static Expression exp(String expressionString, Object... parameters) {
        Expression e = fromString(expressionString);

//        if (parameters != null && parameters.length > 0) {
//            // apply parameters in-place... it is wasteful to clone the
//            // expression that hasn't been exposed to the callers
//            e.inPlaceParamsArray(parameters);
//        }

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
     * @since 4.1
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
//        for (int i = 0; i < len; i++) {
//            exp.setOperand(i, expressions[i]);
//        }
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
     * @see ExpressionFactory#matchExp(String, Object)
     */
    static Expression matchExp(Expression exp, Object value) {
        if(!(exp instanceof SimpleNode)) {
            throw new IllegalArgumentException("exp should be instance of SimpleNode");
        }
        return new ASTEqual((SimpleNode)exp, value);
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
     *
     * @since 4.0
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
