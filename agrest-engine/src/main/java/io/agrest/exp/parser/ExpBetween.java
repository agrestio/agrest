/* Generated By:JJTree: Do not edit this line. ExpBetween.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;

public
class ExpBetween extends AgExpression {
  public ExpBetween(int id) {
    super(id);
  }

  public ExpBetween(AgExpressionParser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected AgExpression shallowCopy() {
    return new ExpBetween(id);
  }

  @Override
  public String toString() {
    return children[0] + " between " + children[1] + " and " + children[2];
  }
}
/* JavaCC - OriginalChecksum=c43b74b247f96290e1207f5b0fcef774 (do not edit this line) */
