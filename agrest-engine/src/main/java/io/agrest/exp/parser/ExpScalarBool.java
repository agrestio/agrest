/* Generated By:JJTree: Do not edit this line. ExpScalarBool.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public class ExpScalarBool extends ExpScalar<Boolean> {
    public ExpScalarBool(int id) {
        super(id);
    }

    public ExpScalarBool(AgExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=4fd68f472c88fe28fc509bfd5da4d00e (do not edit this line) */