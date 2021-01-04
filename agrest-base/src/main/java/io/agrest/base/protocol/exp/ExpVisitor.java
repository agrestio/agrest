package io.agrest.base.protocol.exp;

/**
 * @since 4.4
 */
public interface ExpVisitor {

    void visitSimpleExp(SimpleExp exp);

    void visitNamedParamsExp(NamedParamsExp exp);

    void visitPositionalParamsExp(PositionalParamsExp exp);

    void visitCompositeExp(CompositeExp exp);
}
