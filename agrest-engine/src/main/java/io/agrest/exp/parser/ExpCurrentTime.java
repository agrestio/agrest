/* Generated By:JJTree: Do not edit this line. ExpCurrentTime.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpCurrentTime extends SimpleNode {
  public ExpCurrentTime(int id) {
    super(id);
  }

  public ExpCurrentTime(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpCurrentTime() {
    super(AgExpressionParserTreeConstants.JJTCURRENTTIME);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected SimpleNode shallowCopy() {
    return new ExpCurrentTime(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=1f84b3754ed74e9ae5b7eca31d84f0ae (do not edit this line) */
