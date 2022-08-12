/* Generated By:JavaCC: Do not edit this line. AgExpressionParserDefaultVisitor.java Version 7.0.11 */
package io.agrest.exp.parser;

public class AgExpressionParserDefaultVisitor<T> implements AgExpressionParserVisitor<T>{
  public T defaultVisit(SimpleNode node, T data){
    node.childrenAccept(this, data);
    return data;
  }
  public T visit(SimpleNode node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpRoot node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpOr node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpAnd node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNot node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpTrue node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpFalse node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpEqual node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNotEqual node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLessOrEqual node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLess node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpGreater node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpGreaterOrEqual node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLike node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLikeIgnoreCase node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpIn node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBetween node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNotLike node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNotLikeIgnoreCase node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNotIn node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNotBetween node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarList node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarNull node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarString node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarBool node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseOr node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseXor node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseAnd node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseLeftShift node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseRightShift node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpAdd node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpSubtract node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpMultiply node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpDivide node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpBitwiseNot node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNegate node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarInt node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpScalarFloat node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpConcat node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpSubstring node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpTrim node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLower node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpUpper node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLength node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpLocate node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpAbs node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpSqrt node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpMod node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpCurrentDate node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpCurrentTime node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpCurrentTimestamp node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpExtract node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpNamedParameter node, T data){
    return defaultVisit(node, data);
  }
  public T visit(ExpObjPath node, T data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=5ad65aa2447293df9168f4677ec559b7 (do not edit this line) */