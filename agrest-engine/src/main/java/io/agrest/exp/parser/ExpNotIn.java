/* Generated By:JJTree: Do not edit this line. ExpNotIn.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpNotIn extends SimpleNode {
  public ExpNotIn(int id) {
    super(id);
  }

  public ExpNotIn(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpNotIn() {
    super(AgExpressionParserTreeConstants.JJTNOTIN);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpNotIn shallowCopy() {
    return new ExpNotIn(id);
  }

  // TODO: override not() to compact the result

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=49ec5ffe4a961c9a3e6170dbd0f7ad1f (do not edit this line) */
