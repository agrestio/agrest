package io.agrest.exp;

/**
 * @since 4.4
 * @deprecated in favor of {@link io.agrest.exp.parser.AgExpressionParserVisitor}
 */
@Deprecated(since = "5.0")
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
