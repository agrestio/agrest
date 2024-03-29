/* Generated By:JJTree: Do not edit this line. ExpLower.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpLower extends SimpleNode {
  public ExpLower(int id) {
    super(id);
  }

  public ExpLower(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpLower() {
    super(AgExpressionParserTreeConstants.JJTLOWER);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected SimpleNode shallowCopy() {
    return new ExpLower(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=46a8609164b2bd56451f930c7b2db122 (do not edit this line) */
