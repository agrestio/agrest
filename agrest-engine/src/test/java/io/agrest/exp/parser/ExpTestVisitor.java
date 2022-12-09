package io.agrest.exp.parser;

class ExpTestVisitor extends AgExpressionParserDefaultVisitor<Class<?>> {

    private final Class<? extends SimpleNode> nodeType;

    public ExpTestVisitor(Class<? extends SimpleNode> nodeType) {
        this.nodeType = nodeType;
    }

    public Class<? extends SimpleNode> getNodeType() {
        return nodeType;
    }

    @Override
    public Class<?> visit(ExpRoot node, Class<?> data) {
        return node.children[0].getClass();
    }
}
