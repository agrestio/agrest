/* Generated By:JJTree: Do not edit this line. ExpAbs.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpAbs extends SimpleNode {
  public ExpAbs(int id) {
    super(id);
  }

  public ExpAbs(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpAbs() {
    super(AgExpressionParserTreeConstants.JJTABS);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected SimpleNode shallowCopy() {
    return new ExpAbs(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=01d2266f2e2852f0cc30cb38133844d2 (do not edit this line) */
