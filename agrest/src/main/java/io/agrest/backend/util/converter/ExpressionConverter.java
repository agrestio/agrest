package io.agrest.backend.util.converter;

import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.parser.SimpleNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public interface ExpressionConverter<B> extends Converter<Expression, B> {

    @Override
    B convert(Expression from);

    default List<B> convertChildren(Expression from, ExpressionConverter<B> converter) {
        List<B> result = new ArrayList<>();

        SimpleNode fromNode = (SimpleNode)from;
        for (int i = 0; i < fromNode.jjtGetNumChildren(); i++) {
            result.add(converter.convert((Expression)fromNode.jjtGetChild(i)));
        }
        return result;
    }
}
