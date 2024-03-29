/* Generated By:JJTree: Do not edit this line. ExpSqrt.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpSqrt extends SimpleNode {
  public ExpSqrt(int id) {
    super(id);
  }

  public ExpSqrt(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpSqrt() {
    super(AgExpressionParserTreeConstants.JJTSQRT);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpSqrt shallowCopy() {
    return new ExpSqrt(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=e749d50dc99d6d7acc161593f5e1b43e (do not edit this line) */
