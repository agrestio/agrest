package io.agrest.cayenne.exp;

import io.agrest.cayenne.path.PathOps;
import io.agrest.exp.parser.SimpleNode;
import io.agrest.exp.parser.*;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionParameter;
import org.apache.cayenne.exp.parser.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

class CayenneExpressionVisitor implements AgExpressionParserVisitor<Expression> {

    @Override
    public Expression visit(SimpleNode node, Expression data) {
        return node.jjtAccept(this, data);
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
        // NOTE: we are skipping all the List children as they are processed by the getValue() call
        Collection<?> values = node.getValue();
        List<Object> preparedValues = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof CharSequence)) {
                preparedValues.add(value);
                continue;
            }
            String stringValue = value.toString();
            preparedValues.add(stringValue.substring(1, stringValue.length() - 1));
        }

        Object[] cayenneValues = new Object[preparedValues.size()];
        int i = 0;
        for (Object next : preparedValues) {
            if (next instanceof ExpNamedParameter) {
                cayenneValues[i++] = new ExpressionParameter(((ExpNamedParameter) next).getName());
            } else {
                cayenneValues[i++] = next;
            }
        }

        ASTList list = new ASTList(cayenneValues);
        if (data != null) {
            data.setOperand(data.getOperandCount(), list);
            return data;
        }
        return list;
    }

    @Override
    public Expression visit(ExpScalar node, Expression data) {
        Object scalarValue = node.jjtGetValue();
        if (scalarValue instanceof CharSequence) {
            String value = scalarValue.toString();
            return process(node, data, new ASTScalar(value.substring(1, value.length() - 1)));
        }
        return process(node, data, new ASTScalar(scalarValue));
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
    public Expression visit(ExpPath node, Expression parent) {
        ASTPath path = PathOps.parsePath((String) node.jjtGetValue());
        return process(node, parent, path);
    }

    private Expression process(SimpleNode node, Expression parent, Expression exp) {
        if (node.jjtGetNumChildren() > 0) {
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                exp = node.jjtGetChild(i).jjtAccept(this, exp);
            }
        }
        if (parent != null) {
            BiFunction<Expression, Expression, Expression> childMerger = getMergerForNode(parent);
            return childMerger.apply(parent, exp);
        } else {
            return exp;
        }
    }

    BiFunction<Expression, Expression, Expression> getMergerForNode(Expression node) {
        if (node instanceof PatternMatchNode) {
            return this::addToLikeNode;
        } else {
            return this::addToParent;
        }
    }

    private Expression addToParent(Expression parent, Expression child) {
        parent.setOperand(parent.getOperandCount(), child);
        return parent;
    }

    private Expression addToLikeNode(Expression parent, Expression child) {
        if (!(parent instanceof PatternMatchNode)) {
            throw new IllegalArgumentException("ParentMatchNode expected, got " + parent.getClass().getSimpleName());
        }
        PatternMatchNode patternMatchNode = (PatternMatchNode) parent;
        if (parent.getOperandCount() == 2) {
            if (!(child instanceof ASTScalar)) {
                throw new IllegalArgumentException("ASTScalar expected, got " + child.getClass().getSimpleName());
            }
            String escape = ((ASTScalar) child).getValue().toString();
            if (escape.length() != 1) {
                throw new IllegalArgumentException("Single escape char expected, got '" + escape + "'");
            }
            patternMatchNode.setEscapeChar(escape.charAt(0));
            return parent;
        } else {
            return addToParent(parent, child);
        }
    }

    // A hack - must use reflection to create Cayenne expressions, as the common int constructor is not public
    // in any of them.
    // TODO: refactor this in Cayenne to provide public constructors
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