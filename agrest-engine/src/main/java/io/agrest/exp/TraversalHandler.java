package io.agrest.exp;

import io.agrest.exp.parser.SimpleNode;

public interface TraversalHandler {

    default void startNode(SimpleNode node, SimpleNode parentNode) {
    }

    default void endNode(SimpleNode node, SimpleNode parentNode) {
    }

    default void finishedChild(SimpleNode node, int childIndex, boolean hasMoreChildren) {
    }

    default void objectNode(Object leaf, SimpleNode parentNode) {
    }
}
