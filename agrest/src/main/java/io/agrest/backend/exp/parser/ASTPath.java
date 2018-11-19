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

    @Override
    public Object getOperand(int index) {
        if (index == 0) {
            return path;
        }

        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getPath() {
        return path;
    }

}
