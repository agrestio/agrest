/* Generated By:JJTree: Do not edit this line. ExpExists.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.protocol.Exp;

public
class ExpExists extends SimpleNode {
  public ExpExists(int id) {
    super(id);
  }

  public ExpExists(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpExists() {
    super(AgExpressionParser.JJTEXISTS);
  }

  public ExpExists(Exp subExp) {
      this();
      setOperand(0, subExp);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected SimpleNode shallowCopy() {
    return new ExpExists(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=c9bebed7c12d1268a26003897ca9de6a (do not edit this line) */
