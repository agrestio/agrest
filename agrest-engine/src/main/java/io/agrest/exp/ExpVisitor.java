package io.agrest.exp;

/**
 * @since 4.4
 * @deprecated since 5.0, use {@link io.agrest.exp.parser.AgExpressionParserVisitor} instead
 */
@Deprecated
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
