/* Generated By:JJTree: Do not edit this line. ExpGreaterOrEqual.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpGreaterOrEqual extends SimpleNode {
  public ExpGreaterOrEqual(int id) {
    super(id);
  }

  public ExpGreaterOrEqual(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpGreaterOrEqual() {
    super(AgExpressionParserTreeConstants.JJTGREATEROREQUAL);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpGreaterOrEqual shallowCopy() {
    return new ExpGreaterOrEqual(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=723bcd56cadc1762fd2460f1ab9c988f (do not edit this line) */
