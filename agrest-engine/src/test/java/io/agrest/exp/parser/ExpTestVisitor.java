package io.agrest.exp.parser;

import java.util.function.BooleanSupplier;

class ExpTestVisitor extends AgExpressionParserDefaultVisitor<BooleanSupplier> {
    @Override
    public BooleanSupplier visit(ExpRoot node, BooleanSupplier data) {
        return node.children[0].accept(this, data);
    }
}
