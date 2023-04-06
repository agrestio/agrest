package io.agrest.exp;

public interface TraversalHandler {

    void startNode(AgExpression node, AgExpression parentNode);

    void endNode(AgExpression node, AgExpression parentNode);

    void finishedChild(AgExpression node, int childIndex, boolean hasMoreChildren);

    void objectNode(Object leaf, AgExpression parentNode);
}
