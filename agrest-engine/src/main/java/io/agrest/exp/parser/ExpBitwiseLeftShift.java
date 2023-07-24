/* Generated By:JJTree: Do not edit this line. ExpBitwiseLeftShift.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;

public
class ExpBitwiseLeftShift extends AgExpression {
  public ExpBitwiseLeftShift(int id) {
    super(id);
  }

  public ExpBitwiseLeftShift(AgExpressionParser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected AgExpression shallowCopy() {
    return new ExpBitwiseLeftShift(id);
  }

  @Override
  public String toString() {
    return "(" + children[0] + ") << (" + children[1] + ")";
  }
}
/* JavaCC - OriginalChecksum=a6ef6adcc35a4fc93a897a3b9a1b6dfd (do not edit this line) */
