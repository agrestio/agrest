package io.agrest.exp;

/**
 * @since 4.4
 */
public interface ExpVisitor {

    void visitSimpleExp(SimpleExp exp);

    void visitNamedParamsExp(NamedParamsExp exp);

    void visitPositionalParamsExp(PositionalParamsExp exp);

    /**
     * @since 5.0
     */
    void visitKeyValueExp(KeyValueExp exp);

    void visitCompositeExp(CompositeExp exp);
}
