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


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=78023c421e8beee787e850bd8ccf86fd (do not edit this line) */
