package io.agrest.exp.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpBuilder {

    private static final Map<Class<? extends SimpleNode>, Integer> expIdMap = new HashMap<>();

    static {
        expIdMap.put(ExpRoot.class, AgExpressionParserTreeConstants.JJTROOT);
        expIdMap.put(ExpOr.class, AgExpressionParserTreeConstants.JJTOR);
        expIdMap.put(ExpAnd.class, AgExpressionParserTreeConstants.JJTAND);
        expIdMap.put(ExpNot.class, AgExpressionParserTreeConstants.JJTNOT);
        expIdMap.put(ExpTrue.class, AgExpressionParserTreeConstants.JJTTRUE);
        expIdMap.put(ExpFalse.class, AgExpressionParserTreeConstants.JJTFALSE);
        expIdMap.put(ExpEqual.class, AgExpressionParserTreeConstants.JJTEQUAL);
        expIdMap.put(ExpNotEqual.class, AgExpressionParserTreeConstants.JJTNOTEQUAL);
        expIdMap.put(ExpLessOrEqual.class, AgExpressionParserTreeConstants.JJTLESSOREQUAL);
        expIdMap.put(ExpLess.class, AgExpressionParserTreeConstants.JJTLESS);
        expIdMap.put(ExpGreater.class, AgExpressionParserTreeConstants.JJTGREATER);
        expIdMap.put(ExpGreaterOrEqual.class, AgExpressionParserTreeConstants.JJTGREATEROREQUAL);
        expIdMap.put(ExpLike.class, AgExpressionParserTreeConstants.JJTLIKE);
        expIdMap.put(ExpLikeIgnoreCase.class, AgExpressionParserTreeConstants.JJTLIKEIGNORECASE);
        expIdMap.put(ExpIn.class, AgExpressionParserTreeConstants.JJTIN);
        expIdMap.put(ExpBetween.class, AgExpressionParserTreeConstants.JJTBETWEEN);
        expIdMap.put(ExpNotLike.class, AgExpressionParserTreeConstants.JJTNOTLIKE);
        expIdMap.put(ExpNotLikeIgnoreCase.class, AgExpressionParserTreeConstants.JJTNOTLIKEIGNORECASE);
        expIdMap.put(ExpNotIn.class, AgExpressionParserTreeConstants.JJTNOTIN);
        expIdMap.put(ExpNotBetween.class, AgExpressionParserTreeConstants.JJTNOTBETWEEN);
        expIdMap.put(ExpScalarList.class, AgExpressionParserTreeConstants.JJTSCALARLIST);
        expIdMap.put(ExpScalarNull.class, AgExpressionParserTreeConstants.JJTSCALARNULL);
        expIdMap.put(ExpScalarString.class, AgExpressionParserTreeConstants.JJTSCALARSTRING);
        expIdMap.put(ExpScalarBool.class, AgExpressionParserTreeConstants.JJTSCALARBOOL);
        expIdMap.put(ExpBitwiseOr.class, AgExpressionParserTreeConstants.JJTBITWISEOR);
        expIdMap.put(ExpBitwiseXor.class, AgExpressionParserTreeConstants.JJTBITWISEXOR);
        expIdMap.put(ExpBitwiseAnd.class, AgExpressionParserTreeConstants.JJTBITWISEAND);
        expIdMap.put(ExpBitwiseLeftShift.class, AgExpressionParserTreeConstants.JJTBITWISELEFTSHIFT);
        expIdMap.put(ExpBitwiseRightShift.class, AgExpressionParserTreeConstants.JJTBITWISERIGHTSHIFT);
        expIdMap.put(ExpAdd.class, AgExpressionParserTreeConstants.JJTADD);
        expIdMap.put(ExpSubtract.class, AgExpressionParserTreeConstants.JJTSUBTRACT);
        expIdMap.put(ExpMultiply.class, AgExpressionParserTreeConstants.JJTMULTIPLY);
        expIdMap.put(ExpDivide.class, AgExpressionParserTreeConstants.JJTDIVIDE);
        expIdMap.put(ExpBitwiseNot.class, AgExpressionParserTreeConstants.JJTBITWISENOT);
        expIdMap.put(ExpNegate.class, AgExpressionParserTreeConstants.JJTNEGATE);
        expIdMap.put(ExpScalarInt.class, AgExpressionParserTreeConstants.JJTSCALARINT);
        expIdMap.put(ExpScalarFloat.class, AgExpressionParserTreeConstants.JJTSCALARFLOAT);
        expIdMap.put(ExpConcat.class, AgExpressionParserTreeConstants.JJTCONCAT);
        expIdMap.put(ExpSubstring.class, AgExpressionParserTreeConstants.JJTSUBSTRING);
        expIdMap.put(ExpTrim.class, AgExpressionParserTreeConstants.JJTTRIM);
        expIdMap.put(ExpLower.class, AgExpressionParserTreeConstants.JJTLOWER);
        expIdMap.put(ExpUpper.class, AgExpressionParserTreeConstants.JJTUPPER);
        expIdMap.put(ExpLength.class, AgExpressionParserTreeConstants.JJTLENGTH);
        expIdMap.put(ExpLocate.class, AgExpressionParserTreeConstants.JJTLOCATE);
        expIdMap.put(ExpAbs.class, AgExpressionParserTreeConstants.JJTABS);
        expIdMap.put(ExpSqrt.class, AgExpressionParserTreeConstants.JJTSQRT);
        expIdMap.put(ExpMod.class, AgExpressionParserTreeConstants.JJTMOD);
        expIdMap.put(ExpCurrentDate.class, AgExpressionParserTreeConstants.JJTCURRENTDATE);
        expIdMap.put(ExpCurrentTime.class, AgExpressionParserTreeConstants.JJTCURRENTTIME);
        expIdMap.put(ExpCurrentTimestamp.class, AgExpressionParserTreeConstants.JJTCURRENTTIMESTAMP);
        expIdMap.put(ExpExtract.class, AgExpressionParserTreeConstants.JJTEXTRACT);
        expIdMap.put(ExpNamedParameter.class, AgExpressionParserTreeConstants.JJTNAMEDPARAMETER);
        expIdMap.put(ExpObjPath.class, AgExpressionParserTreeConstants.JJTOBJPATH);
    }

    private final SimpleNode exp;
    private final List<ExpBuilder> children = new ArrayList<>();
    private Object value;
    private Object[] positionalParams;
    private Map<String, Object> namedParams;
    private SimpleNode parent;

    public ExpBuilder(Class<? extends SimpleNode> expType) {
        try {
            exp = expType.getConstructor(int.class).newInstance(expIdMap.get(expType));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ExpBuilder fromType(Class<? extends SimpleNode> expType) {
        return new ExpBuilder(expType);
    }

    public ExpBuilder withPositionalParams(Object... params) {
        positionalParams = params;
        return this;
    }

    public ExpBuilder withNamedParams(Map<String, Object> params) {
        namedParams = params;
        return this;
    }

    public ExpBuilder withValue(Object value) {
        this.value = value;
        return this;
    }

    public ExpBuilder addChild(ExpBuilder childBuilder) {
        children.add(childBuilder);
        childBuilder.parent = exp;
        return this;
    }

    public SimpleNode build() {
        if (exp.getClass() == ExpRoot.class) {
            ExpRoot expRoot = (ExpRoot) exp;
            expRoot.positionalParams(positionalParams);
            expRoot.namedParams(namedParams);
        }

        SimpleNode[] children = this.children.stream().map(ExpBuilder::build).toArray(SimpleNode[]::new);
        if (children.length != 0) {
            exp.children = children;
        }
        exp.parent = parent;
        exp.value = value;
        return exp;
    }
}
