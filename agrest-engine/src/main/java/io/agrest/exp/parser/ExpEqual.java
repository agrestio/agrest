/* Generated By:JJTree: Do not edit this line. ExpEqual.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public class ExpEqual extends SimpleNode {
    public ExpEqual(int id) {
        super(id);
    }

    public ExpEqual(AgExpressionParser p, int id) {
        super(p, id);
    }

    public static ExpEqual of(ExpObjPath path, ExpScalar<?> value) {
        ExpEqual equal = new ExpEqual(AgExpressionParserTreeConstants.JJTEQUAL);
        equal.jjtAddChild(path, 0);
        equal.jjtAddChild(value, 1);
        return equal;
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=f7902564cc83bb81157b4fd462a410ff (do not edit this line) */