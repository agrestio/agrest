package io.agrest.runtime.cayenne.converter;

import io.agrest.backend.exp.ExpressionException;
import io.agrest.backend.util.converter.ExpressionConverter;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTList;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTScalar;
import org.apache.cayenne.exp.parser.SimpleNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class CayenneExpressionConverter implements ExpressionConverter<Expression> {


    @Override
    public Expression convert(io.agrest.backend.exp.Expression from) {
        if (from == null) {
            return null;
        }

        io.agrest.backend.exp.parser.SimpleNode fromNode = (io.agrest.backend.exp.parser.SimpleNode)from;

        SimpleNode result = null;

        try {
            result = (SimpleNode) Class.forName(
                    Expression.class.getPackage().getName() + ".parser." + fromNode.getClass().getSimpleName())
                    .newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ExpressionException("Could not convert %s to Cayenne Expression ", fromNode.getClass());
        }

        if (result != null) {
            List<Expression> children = convertChildren(fromNode, this);

            if (from instanceof io.agrest.backend.exp.parser.ASTScalar) {
                ((ASTScalar)result).setValue(((io.agrest.backend.exp.parser.ASTScalar)from).getValue());
            } else if (from instanceof io.agrest.backend.exp.parser.ASTDbPath) {
                result = new ASTDbPath(((io.agrest.backend.exp.parser.ASTDbPath)from).getPath());
            } else if (from instanceof io.agrest.backend.exp.parser.ASTObjPath) {
                result = new ASTObjPath(((io.agrest.backend.exp.parser.ASTObjPath)from).getPath());
            } else if (from instanceof io.agrest.backend.exp.parser.ASTList) {
                Object[] values = ((io.agrest.backend.exp.parser.ASTList)from).getValues();
                if (values == null) {
                    result = new ASTList(children.stream().map(c -> ((ASTScalar)c).getValue()).collect(Collectors.toList()));
                } else {
                    result = new ASTList(values);
                }
            }

            result.setType(fromNode.getType());
            for (int i = 0; i < children.size(); i++) {
                result.jjtAddChild((SimpleNode)children.get(i), i);
            }

        }

        return result;
    }
}
