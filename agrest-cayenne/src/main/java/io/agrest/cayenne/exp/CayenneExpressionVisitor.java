package io.agrest.cayenne.exp;

import java.lang.reflect.Constructor;
import java.util.Collection;

import io.agrest.cayenne.path.PathOps;
import io.agrest.exp.parser.*;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionParameter;
import org.apache.cayenne.exp.parser.ASTAbs;
import org.apache.cayenne.exp.parser.ASTAdd;
import org.apache.cayenne.exp.parser.ASTAnd;
import org.apache.cayenne.exp.parser.ASTBetween;
import org.apache.cayenne.exp.parser.ASTBitwiseAnd;
import org.apache.cayenne.exp.parser.ASTBitwiseLeftShift;
import org.apache.cayenne.exp.parser.ASTBitwiseNot;
import org.apache.cayenne.exp.parser.ASTBitwiseOr;
import org.apache.cayenne.exp.parser.ASTBitwiseRightShift;
import org.apache.cayenne.exp.parser.ASTBitwiseXor;
import org.apache.cayenne.exp.parser.ASTConcat;
import org.apache.cayenne.exp.parser.ASTCurrentDate;
import org.apache.cayenne.exp.parser.ASTCurrentTime;
import org.apache.cayenne.exp.parser.ASTCurrentTimestamp;
import org.apache.cayenne.exp.parser.ASTDivide;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTExtract;
import org.apache.cayenne.exp.parser.ASTFalse;
import org.apache.cayenne.exp.parser.ASTGreater;
import org.apache.cayenne.exp.parser.ASTGreaterOrEqual;
import org.apache.cayenne.exp.parser.ASTIn;
import org.apache.cayenne.exp.parser.ASTLength;
import org.apache.cayenne.exp.parser.ASTLess;
import org.apache.cayenne.exp.parser.ASTLessOrEqual;
import org.apache.cayenne.exp.parser.ASTLike;
import org.apache.cayenne.exp.parser.ASTLikeIgnoreCase;
import org.apache.cayenne.exp.parser.ASTList;
import org.apache.cayenne.exp.parser.ASTLocate;
import org.apache.cayenne.exp.parser.ASTLower;
import org.apache.cayenne.exp.parser.ASTMod;
import org.apache.cayenne.exp.parser.ASTMultiply;
import org.apache.cayenne.exp.parser.ASTNamedParameter;
import org.apache.cayenne.exp.parser.ASTNegate;
import org.apache.cayenne.exp.parser.ASTNot;
import org.apache.cayenne.exp.parser.ASTNotBetween;
import org.apache.cayenne.exp.parser.ASTNotEqual;
import org.apache.cayenne.exp.parser.ASTNotIn;
import org.apache.cayenne.exp.parser.ASTNotLike;
import org.apache.cayenne.exp.parser.ASTNotLikeIgnoreCase;
import org.apache.cayenne.exp.parser.ASTOr;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.parser.ASTScalar;
import org.apache.cayenne.exp.parser.ASTSqrt;
import org.apache.cayenne.exp.parser.ASTSubstring;
import org.apache.cayenne.exp.parser.ASTSubtract;
import org.apache.cayenne.exp.parser.ASTTrim;
import org.apache.cayenne.exp.parser.ASTTrue;
import org.apache.cayenne.exp.parser.ASTUpper;

class CayenneExpressionVisitor implements AgExpressionParserVisitor<Expression> {

    @Override
    public Expression visit(SimpleNode node, Expression data) {
        return node.jjtAccept(this, data);
    }

    @Override
    public Expression visit(ExpRoot node, Expression data) {
        if (node.jjtGetNumChildren() == 0) {
            return null;
        }
        Expression expression = node.jjtGetChild(0).jjtAccept(this, data);
        if(node.hasNamedParams()) {
            return expression.params(node.getNamedParams());
        } else if(node.hasPositionalParams()) {
            return expression.paramsArray(node.getPositionalParams());
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(ExpOr node, Expression parent) {
        return process(node, parent, new ASTOr());
    }

    @Override
    public Expression visit(ExpAnd node, Expression data) {
        return process(node, data, new ASTAnd());
    }

    @Override
    public Expression visit(ExpNot node, Expression data) {
        return process(node, data, new ASTNot());
    }

    @Override
    public Expression visit(ExpTrue node, Expression data) {
        return process(node, data, new ASTTrue());
    }

    @Override
    public Expression visit(ExpFalse node, Expression data) {
        return process(node, data, new ASTFalse());
    }

    @Override
    public Expression visit(ExpEqual node, Expression parent) {
        return process(node, parent, new ASTEqual());
    }

    @Override
    public Expression visit(ExpNotEqual node, Expression data) {
        return process(node, data, new ASTNotEqual());
    }

    @Override
    public Expression visit(ExpLessOrEqual node, Expression data) {
        return process(node, data, new ASTLessOrEqual());
    }

    @Override
    public Expression visit(ExpLess node, Expression data) {
        return process(node, data, new ASTLess());
    }

    @Override
    public Expression visit(ExpGreater node, Expression data) {
        return process(node, data, new ASTGreater());
    }

    @Override
    public Expression visit(ExpGreaterOrEqual node, Expression data) {
        return process(node, data, new ASTGreaterOrEqual());
    }

    @Override
    public Expression visit(ExpLike node, Expression data) {
        return process(node, data, new ASTLike());
    }

    @Override
    public Expression visit(ExpLikeIgnoreCase node, Expression data) {
        return process(node, data, new ASTLikeIgnoreCase());
    }

    @Override
    public Expression visit(ExpIn node, Expression data) {
        return process(node, data, new ASTIn());
    }

    @Override
    public Expression visit(ExpBetween node, Expression data) {
        return process(node, data, new ASTBetween());
    }

    @Override
    public Expression visit(ExpNotLike node, Expression data) {
        return process(node, data, new ASTNotLike());
    }

    @Override
    public Expression visit(ExpNotLikeIgnoreCase node, Expression data) {
        return process(node, data, new ASTNotLikeIgnoreCase());
    }

    @Override
    public Expression visit(ExpNotIn node, Expression data) {
        return process(node, data, new ASTNotIn());
    }

    @Override
    public Expression visit(ExpNotBetween node, Expression data) {
        return process(node, data, new ASTNotBetween());
    }

    @Override
    public Expression visit(ExpScalarList node, Expression data) {
        // NOTE: we are skipping all the List children as there are processed by the getValue() call
        Collection<?> value = node.getValue();
        Object[] cayenneValues = new Object[value.size()];
        int i = 0;
        for(Object next : value) {
            if(next instanceof NamedParameter) {
                cayenneValues[i++] = new ExpressionParameter(((NamedParameter) next).getName());
            } else {
                cayenneValues[i++] = next;
            }
        }

        ASTList list = new ASTList(cayenneValues);
        if(data != null) {
            data.setOperand(data.getOperandCount(), list);
            return data;
        }
        return list;
    }

    @Override
    public Expression visit(ExpScalarNull node, Expression data) {
        return process(node, data, new ASTScalar(null));
    }

    @Override
    public Expression visit(ExpScalarString node, Expression data) {
        return process(node, data, new ASTScalar(node.jjtGetValue()));
    }

    @Override
    public Expression visit(ExpScalarBool node, Expression data) {
        return process(node, data, new ASTScalar(node.jjtGetValue()));
    }

    @Override
    public Expression visit(ExpBitwiseOr node, Expression data) {
        return process(node, data, new ASTBitwiseOr());
    }

    @Override
    public Expression visit(ExpBitwiseXor node, Expression data) {
        return process(node, data, new ASTBitwiseXor());
    }

    @Override
    public Expression visit(ExpBitwiseAnd node, Expression data) {
        return process(node, data, new ASTBitwiseAnd());
    }

    @Override
    public Expression visit(ExpBitwiseLeftShift node, Expression data) {
        return process(node, data, new ASTBitwiseLeftShift());
    }

    @Override
    public Expression visit(ExpBitwiseRightShift node, Expression data) {
        return process(node, data, new ASTBitwiseRightShift());
    }

    @Override
    public Expression visit(ExpAdd node, Expression data) {
        return process(node, data, new ASTAdd());
    }

    @Override
    public Expression visit(ExpSubtract node, Expression data) {
        return process(node, data, new ASTSubtract());
    }

    @Override
    public Expression visit(ExpMultiply node, Expression data) {
        return process(node, data, new ASTMultiply());
    }

    @Override
    public Expression visit(ExpDivide node, Expression data) {
        return process(node, data, new ASTDivide());
    }

    @Override
    public Expression visit(ExpBitwiseNot node, Expression data) {
        return process(node, data, new ASTBitwiseNot());
    }

    @Override
    public Expression visit(ExpNegate node, Expression data) {
        return process(node, data, new ASTNegate());
    }

    @Override
    public Expression visit(ExpScalarInt node, Expression parent) {
        return process(node, parent, new ASTScalar(node.jjtGetValue()));
    }

    @Override
    public Expression visit(ExpScalarFloat node, Expression data) {
        return process(node, data, new ASTScalar(node.jjtGetValue()));
    }

    @Override
    public Expression visit(ExpConcat node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTConcat.class));
    }

    @Override
    public Expression visit(ExpSubstring node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTSubstring.class));
    }

    @Override
    public Expression visit(ExpTrim node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTTrim.class));
    }

    @Override
    public Expression visit(ExpLower node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTLower.class));
    }

    @Override
    public Expression visit(ExpUpper node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTUpper.class));
    }

    @Override
    public Expression visit(ExpLength node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTLength.class));
    }

    @Override
    public Expression visit(ExpLocate node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTLocate.class));
    }

    @Override
    public Expression visit(ExpAbs node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTAbs.class));
    }

    @Override
    public Expression visit(ExpSqrt node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTSqrt.class));
    }

    @Override
    public Expression visit(ExpMod node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTMod.class));
    }

    @Override
    public Expression visit(ExpCurrentDate node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTCurrentDate.class));
    }

    @Override
    public Expression visit(ExpCurrentTime node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTCurrentTime.class));
    }

    @Override
    public Expression visit(ExpCurrentTimestamp node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTCurrentTimestamp.class));
    }

    @Override
    public Expression visit(ExpExtract node, Expression data) {
        // no public constructor that we could use directly
        return process(node, data, constructExpression(ASTExtract.class));
    }

    @Override
    public Expression visit(ExpNamedParameter node, Expression data) {
        return process(node, data, new ASTNamedParameter(node.jjtGetValue()));
    }

    @Override
    public Expression visit(ExpObjPath node, Expression parent) {
        ASTPath path = PathOps.parsePath((String)node.jjtGetValue());
        return process(node, parent, path);
    }

    private Expression processList(SimpleNode node, Expression parent, Expression exp) {
        if (node.jjtGetNumChildren() > 0) {
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                node.jjtGetChild(i).jjtAccept(this, exp);
            }
        }
        return parent;
    }

    private Expression process(SimpleNode node, Expression parent, Expression exp) {
        if (node.jjtGetNumChildren() > 0) {
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                exp = node.jjtGetChild(i).jjtAccept(this, exp);
            }
        }
        if (parent != null) {
            parent.setOperand(parent.getOperandCount(), exp);
            return parent;
        } else {
            return exp;
        }
    }

    private Expression constructExpression(Class<? extends Expression> expClass) {
        Expression exp;
        try {
            Constructor<? extends Expression> constructor = expClass.getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            exp = constructor.newInstance(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return exp;
    }
}