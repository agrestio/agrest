package io.agrest.exp;

public interface TraversalHandler {

    default void startNode(AgExpression node, AgExpression parentNode) {
    }

    default void endNode(AgExpression node, AgExpression parentNode) {
    }

    default void finishedChild(AgExpression node, int childIndex, boolean hasMoreChildren) {
    }

    default void objectNode(Object leaf, AgExpression parentNode) {
    }
}
