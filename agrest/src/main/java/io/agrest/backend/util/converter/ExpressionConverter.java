package io.agrest.backend.util.converter;

import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.parser.SimpleNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 *
 */
public interface ExpressionConverter<R> extends Function<Expression, R> {


    default List<R> convertChildren(Expression from, ExpressionConverter<R> converter) {
        List<R> result = new ArrayList<>();

        SimpleNode fromNode = (SimpleNode)from;
        for (int i = 0; i < fromNode.jjtGetNumChildren(); i++) {
            result.add(converter.apply((Expression)fromNode.jjtGetChild(i)));
        }
        return result;
    }
}
