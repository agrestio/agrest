package io.agrest.exp.parser;

class ExpTestVisitor extends AgExpressionParserDefaultVisitor<Class<?>> {

    private final Class<? extends SimpleNode> nodeType;

    public ExpTestVisitor(Class<? extends SimpleNode> nodeType) {
        this.nodeType = nodeType;
    }

    public Class<? extends SimpleNode> getNodeType() {
        return nodeType;
    }
}
