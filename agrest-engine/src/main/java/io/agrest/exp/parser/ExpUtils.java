package io.agrest.exp.parser;

public class ExpUtils {

    public static Node in(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpIn(AgExpressionParserTreeConstants.JJTIN), path, scalar);
    }

    public static Node likeIgnoreCase(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpLikeIgnoreCase(AgExpressionParserTreeConstants.JJTLIKEIGNORECASE), path, scalar);
    }

    public static Node like(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpLike(AgExpressionParserTreeConstants.JJTLIKE), path, scalar);
    }

    public static Node greaterOrEqual(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpGreaterOrEqual(AgExpressionParserTreeConstants.JJTGREATEROREQUAL), path, scalar);
    }

    public static Node lessOrEqual(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpLessOrEqual(AgExpressionParserTreeConstants.JJTLESSOREQUAL), path, scalar);
    }

    public static Node greater(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpGreater(AgExpressionParserTreeConstants.JJTGREATER), path, scalar);
    }

    public static Node less(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpLess(AgExpressionParserTreeConstants.JJTLESS), path, scalar);
    }

    public static Node equal(ExpObjPath path, ExpScalar<?> scalar) {
        return compose(new ExpEqual(AgExpressionParserTreeConstants.JJTEQUAL), path, scalar);
    }

    public static Node and(Node exp1, Node exp2) {
        return compose(new ExpAnd(AgExpressionParserTreeConstants.JJTAND), exp1, exp2);
    }

    public static Node or(Node exp1, Node exp2) {
        return compose(new ExpOr(AgExpressionParserTreeConstants.JJTOR), exp1, exp2);
    }

    private static Node compose(Node exp, Node arg1, Node arg2) {
        exp.jjtAddChild(arg1, 0);
        exp.jjtAddChild(arg2, 1);
        return exp;
    }
}
