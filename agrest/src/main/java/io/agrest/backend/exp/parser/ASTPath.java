package io.agrest.backend.exp.parser;

import org.apache.cayenne.exp.parser.SimpleNode;

/**
 * @author vyarmolovich
 * 10/29/18
 */
public abstract class ASTPath  extends SimpleNode {

    protected ASTPath(int i) {
        super(i);
    }
}
