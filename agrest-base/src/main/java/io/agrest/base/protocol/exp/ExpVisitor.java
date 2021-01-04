package io.agrest.base.protocol.exp;

/**
 * @since 3.8
 */
public interface ExpVisitor {

    void visitSimpleExp(SimpleExp exp);

    void visitNamedParamsExp(NamedParamsExp exp);

    void visitPositionalParamsExp(PositionalParamsExp exp);

    void visitCompositeExp(CompositeExp exp);
}
