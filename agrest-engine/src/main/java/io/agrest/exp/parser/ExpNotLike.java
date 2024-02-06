/* Generated By:JJTree: Do not edit this line. ExpNotLike.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpNotLike extends SimpleNode {
  public ExpNotLike(int id) {
    super(id);
  }

  public ExpNotLike(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpNotLike() {
    super(AgExpressionParserTreeConstants.JJTNOTLIKE);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpNotLike shallowCopy() {
    return new ExpNotLike(id);
  }

  // TODO: override not() to compact the result

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=9551a6b7ade4165e58bcc54093cfb9dd (do not edit this line) */
