package io.agrest.backend.exp.parser;


/**
 * Generic path expression.
 */
public abstract class ASTPath  extends SimpleNode {

    protected String path;

    protected ASTPath(int i) {
        super(i);
    }

    protected void setPath(Object path) {
        this.path = (path != null) ? path.toString() : null;
    }

    public String getPath() {
        return path;
    }

}
