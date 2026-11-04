package io.agrest.cayenne.exp;

import io.agrest.cayenne.path.PathOps;
import io.agrest.exp.AgExpressionException;
import io.agrest.exp.parser.SimpleNode;
import io.agrest.exp.parser.*;
import org.apache.cayenne.exp.AbsExp;
import org.apache.cayenne.exp.AddExp;
import org.apache.cayenne.exp.AndExp;
import org.apache.cayenne.exp.BetweenExp;
import org.apache.cayenne.exp.BitwiseAndExp;
import org.apache.cayenne.exp.BitwiseLeftShiftExp;
import org.apache.cayenne.exp.BitwiseNotExp;
import org.apache.cayenne.exp.BitwiseOrExp;
import org.apache.cayenne.exp.BitwiseRightShiftExp;
import org.apache.cayenne.exp.BitwiseXorExp;
import org.apache.cayenne.exp.ConcatExp;
import org.apache.cayenne.exp.CurrentDateExp;
import org.apache.cayenne.exp.CurrentTimeExp;
import org.apache.cayenne.exp.CurrentTimestampExp;
import org.apache.cayenne.exp.DivideExp;
import org.apache.cayenne.exp.EqualExp;
import org.apache.cayenne.exp.ExistsExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionException;
import org.apache.cayenne.exp.ExpressionParameter;
import org.apache.cayenne.exp.ExtractExp;
import org.apache.cayenne.exp.FalseExp;
import org.apache.cayenne.exp.GreaterExp;
import org.apache.cayenne.exp.GreaterOrEqualExp;
import org.apache.cayenne.exp.InExp;
import org.apache.cayenne.exp.LengthExp;
import org.apache.cayenne.exp.LessExp;
import org.apache.cayenne.exp.LessOrEqualExp;
import org.apache.cayenne.exp.LikeExp;
import org.apache.cayenne.exp.LikeIgnoreCaseExp;
import org.apache.cayenne.exp.ListExp;
import org.apache.cayenne.exp.LocateExp;
import org.apache.cayenne.exp.LowerExp;
import org.apache.cayenne.exp.ModExp;
import org.apache.cayenne.exp.MultiplyExp;
import org.apache.cayenne.exp.NegateExp;
import org.apache.cayenne.exp.NotBetweenExp;
import org.apache.cayenne.exp.NotEqualExp;
import org.apache.cayenne.exp.NotExistsExp;
import org.apache.cayenne.exp.NotExp;
import org.apache.cayenne.exp.NotInExp;
import org.apache.cayenne.exp.NotLikeExp;
import org.apache.cayenne.exp.NotLikeIgnoreCaseExp;
import org.apache.cayenne.exp.OrExp;
import org.apache.cayenne.exp.PathExp;
import org.apache.cayenne.exp.PatternMatchExp;
import org.apache.cayenne.exp.ScalarExp;
import org.apache.cayenne.exp.SqrtExp;
import org.apache.cayenne.exp.SubstringExp;
import org.apache.cayenne.exp.SubtractExp;
import org.apache.cayenne.exp.TrimExp;
import org.apache.cayenne.exp.TrueExp;
import org.apache.cayenne.exp.UpperExp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

/**
 * Converts an Agrest expression tree to a Cayenne expression. Scalar values and named parameters become plain
 * operands of their parent nodes, the same way Cayenne's own parser attaches them. A scalar with no parent is
 * wrapped in a {@link ScalarExp}.
 */
class CayenneExpressionVisitor implements AgExpressionParserVisitor<Expression> {

    @Override
    public Expression visit(SimpleNode node, Expression data) {
        return node.jjtAccept(this, data);
    }

    @Override
    public Expression visit(ExpOr node, Expression parent) {
        return process(node, parent, new OrExp());
    }

    @Override
    public Expression visit(ExpAnd node, Expression data) {
        return process(node, data, new AndExp());
    }

    @Override
    public Expression visit(ExpNot node, Expression data) {
        return process(node, data, new NotExp());
    }

    @Override
    public Expression visit(ExpTrue node, Expression data) {
        return process(node, data, new TrueExp());
    }

    @Override
    public Expression visit(ExpFalse node, Expression data) {
        return process(node, data, new FalseExp());
    }

    @Override
    public Expression visit(ExpEqual node, Expression parent) {
        return process(node, parent, new EqualExp());
    }

    @Override
    public Expression visit(ExpExists node, Expression parent) {
        return process(node, parent, new ExistsExp());
    }

    @Override
    public Expression visit(ExpNotEqual node, Expression data) {
        return process(node, data, new NotEqualExp());
    }

    @Override
    public Expression visit(ExpNotExists node, Expression parent) {
        return process(node, parent, new NotExistsExp());
    }

    @Override
    public Expression visit(ExpLessOrEqual node, Expression data) {
        return process(node, data, new LessOrEqualExp());
    }

    @Override
    public Expression visit(ExpLess node, Expression data) {
        return process(node, data, new LessExp());
    }

    @Override
    public Expression visit(ExpGreater node, Expression data) {
        return process(node, data, new GreaterExp());
    }

    @Override
    public Expression visit(ExpGreaterOrEqual node, Expression data) {
        return process(node, data, new GreaterOrEqualExp());
    }

    @Override
    public Expression visit(ExpLike node, Expression data) {
        return process(node, data, new LikeExp());
    }

    @Override
    public Expression visit(ExpLikeIgnoreCase node, Expression data) {
        return process(node, data, new LikeIgnoreCaseExp());
    }

    @Override
    public Expression visit(ExpIn node, Expression data) {
        return process(node, data, new InExp());
    }

    @Override
    public Expression visit(ExpBetween node, Expression data) {
        return process(node, data, new BetweenExp());
    }

    @Override
    public Expression visit(ExpNotLike node, Expression data) {
        return process(node, data, new NotLikeExp());
    }

    @Override
    public Expression visit(ExpNotLikeIgnoreCase node, Expression data) {
        return process(node, data, new NotLikeIgnoreCaseExp());
    }

    @Override
    public Expression visit(ExpNotIn node, Expression data) {
        return process(node, data, new NotInExp());
    }

    @Override
    public Expression visit(ExpNotBetween node, Expression data) {
        return process(node, data, new NotBetweenExp());
    }

    @Override
    public Expression visit(ExpScalarList node, Expression data) {
        // NOTE: we are skipping all the List children as they are processed by the getValue() call
        Collection<?> value = node.getValue();
        Object[] cayenneValues = new Object[value.size()];
        int i = 0;
        for (Object next : value) {
            if (next instanceof ExpNamedParameter) {
                cayenneValues[i++] = new ExpressionParameter(((ExpNamedParameter) next).getName());
            } else {
                cayenneValues[i++] = next;
            }
        }

        ListExp list = new ListExp(cayenneValues);
        if (data != null) {
            data.setOperand(data.getOperandCount(), list);
            return data;
        }
        return list;
    }

    @Override
    public Expression visit(ExpScalar node, Expression data) {
        return processValue(data, node.jjtGetValue());
    }

    @Override
    public Expression visit(ExpBitwiseOr node, Expression data) {
        return process(node, data, new BitwiseOrExp());
    }

    @Override
    public Expression visit(ExpBitwiseXor node, Expression data) {
        return process(node, data, new BitwiseXorExp());
    }

    @Override
    public Expression visit(ExpBitwiseAnd node, Expression data) {
        return process(node, data, new BitwiseAndExp());
    }

    @Override
    public Expression visit(ExpBitwiseLeftShift node, Expression data) {
        return process(node, data, new BitwiseLeftShiftExp());
    }

    @Override
    public Expression visit(ExpBitwiseRightShift node, Expression data) {
        return process(node, data, new BitwiseRightShiftExp());
    }

    @Override
    public Expression visit(ExpAdd node, Expression data) {
        return process(node, data, new AddExp());
    }

    @Override
    public Expression visit(ExpSubtract node, Expression data) {
        return process(node, data, new SubtractExp());
    }

    @Override
    public Expression visit(ExpMultiply node, Expression data) {
        return process(node, data, new MultiplyExp());
    }

    @Override
    public Expression visit(ExpDivide node, Expression data) {
        return process(node, data, new DivideExp());
    }

    @Override
    public Expression visit(ExpBitwiseNot node, Expression data) {
        return process(node, data, new BitwiseNotExp());
    }

    @Override
    public Expression visit(ExpNegate node, Expression data) {
        if (node.jjtGetNumChildren() == 1 && node.jjtGetChild(0) instanceof ExpScalar) {
            // special case for the negative scalar value, just negate value and skip this node
            return processValue(data, negate(node.jjtGetChild(0).jjtGetValue()));
        }
        return process(node, data, new NegateExp());
    }

    @Override
    public Expression visit(ExpConcat node, Expression data) {
        return process(node, data, new ConcatExp());
    }

    @Override
    public Expression visit(ExpSubstring node, Expression data) {
        return process(node, data, new SubstringExp());
    }

    @Override
    public Expression visit(ExpTrim node, Expression data) {
        return process(node, data, new TrimExp());
    }

    @Override
    public Expression visit(ExpLower node, Expression data) {
        return process(node, data, new LowerExp());
    }

    @Override
    public Expression visit(ExpUpper node, Expression data) {
        return process(node, data, new UpperExp());
    }

    @Override
    public Expression visit(ExpLength node, Expression data) {
        return process(node, data, new LengthExp());
    }

    @Override
    public Expression visit(ExpLocate node, Expression data) {
        return process(node, data, new LocateExp());
    }

    @Override
    public Expression visit(ExpAbs node, Expression data) {
        return process(node, data, new AbsExp());
    }

    @Override
    public Expression visit(ExpSqrt node, Expression data) {
        return process(node, data, new SqrtExp());
    }

    @Override
    public Expression visit(ExpMod node, Expression data) {
        return process(node, data, new ModExp());
    }

    @Override
    public Expression visit(ExpCurrentDate node, Expression data) {
        return process(node, data, new CurrentDateExp());
    }

    @Override
    public Expression visit(ExpCurrentTime node, Expression data) {
        return process(node, data, new CurrentTimeExp());
    }

    @Override
    public Expression visit(ExpCurrentTimestamp node, Expression data) {
        return process(node, data, new CurrentTimestampExp());
    }

    @Override
    public Expression visit(ExpExtract node, Expression data) {
        ExtractExp extract = new ExtractExp();
        extract.setPartToken((String) node.jjtGetValue());
        return process(node, data, extract);
    }

    @Override
    public Expression visit(ExpNamedParameter node, Expression data) {
        return processValue(data, new ExpressionParameter(node.getName()));
    }

    @Override
    public Expression visit(ExpPath node, Expression parent) {
        PathExp path = PathOps.parsePath((String) node.jjtGetValue());
        path.setPathAliases(node.getPathAliases());
        return process(node, parent, path);
    }

    private Expression process(SimpleNode node, Expression parent, Expression exp) {
        for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
            exp = node.jjtGetChild(i).jjtAccept(this, exp);
        }

        return parent != null ? addToParent(parent, exp) : exp;
    }

    /**
     * Attaches a plain (non-expression) value to the parent as an operand, or wraps it in a {@link ScalarExp} if
     * there's no parent.
     */
    private Expression processValue(Expression parent, Object value) {
        return parent != null ? addToParent(parent, value) : new ScalarExp(value);
    }

    private Expression addToParent(Expression parent, Object child) {
        if (parent instanceof PatternMatchExp patternMatch && parent.getOperandCount() == 2) {
            return addEscapeChar(patternMatch, child);
        }

        try {
            parent.setOperand(parent.getOperandCount(), child);
        } catch (ExpressionException e) {
            throw new AgExpressionException(e);
        }
        return parent;
    }

    private Expression addEscapeChar(PatternMatchExp parent, Object child) {
        Object value = child instanceof ScalarExp scalar ? scalar.getValue() : child;
        if (value instanceof Expression) {
            throw new IllegalArgumentException("Scalar expected, got " + value.getClass().getSimpleName());
        }

        String escape = String.valueOf(value);
        if (escape.length() != 1) {
            throw new IllegalArgumentException("Single escape char expected, got '" + escape + "'");
        }
        parent.setEscapeChar(escape.charAt(0));
        return parent;
    }

    Object negate(Object value) {
        return switch (value) {
            case Integer i -> -i;
            case Long l -> -l;
            case Double d -> -d;
            case Float f -> -f;
            case BigInteger bi -> bi.negate();
            case BigDecimal bd -> bd.negate();
            case null, default -> value;
        };
    }
}
