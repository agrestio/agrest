package io.agrest.backend.exp.parser;

import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.ExpressionException;


public abstract class SimpleNode extends Expression implements Node {

    protected Node parent;
    protected Node[] children;
    protected int id;
    protected ExpressionParser parser;

    protected SimpleNode(int i) {
        id = i;
    }

//
//    protected abstract String getExpressionOperator(int index);

    public int getId() {
        return id;
    }

    @Override
    public int getOperandCount() {
        return jjtGetNumChildren();
    }

    @Override
    public void setOperand(int index, Object value) {
        Node node = (value == null || value instanceof Node) ? (Node) value : new ASTScalar(value);
        jjtAddChild(node, index);

        // set the parent, as jjtAddChild doesn't do it...
        if (node != null) {
            node.jjtSetParent(this);
        }
    }

    @Override
    public Object getOperand(int index) {
        Node child = jjtGetChild(index);

        // unwrap ASTScalar nodes - this is likely a temporary thing to keep it compatible
        // with QualifierTranslator. In the future we might want to keep scalar nodes
        // for the purpose of expression evaluation.
        return unwrapChild(child);
    }

    protected Object unwrapChild(Node child) {
        return (child instanceof ASTScalar) ? ((ASTScalar) child).getValue() : child;
    }

    /**
     * Implemented for backwards compatibility with exp package.
     */
    @Override
    public String expName() {
        return ExpressionParserTreeConstants.jjtNodeName[id];
    }

    /**
     * Flattens the tree under this node by eliminating any children that are of
     * the same class as this node and copying their children to this node.
     */
    @Override
    protected void flattenTree() {
        boolean shouldFlatten = false;
        int newSize = 0;

        for (Node child : children) {
            if (child.getClass() == getClass()) {
                shouldFlatten = true;
                newSize += child.jjtGetNumChildren();
            } else {
                newSize++;
            }
        }

        if (shouldFlatten) {
            Node[] newChildren = new Node[newSize];
            int j = 0;

            for (Node c : children) {
                if (c.getClass() == getClass()) {
                    for (int k = 0; k < c.jjtGetNumChildren(); ++k) {
                        newChildren[j++] = c.jjtGetChild(k);
                    }
                } else {
                    newChildren[j++] = c;
                }
            }

            if (j != newSize) {
                throw new ExpressionException("Assertion error: " + j + " != " + newSize);
            }

            this.children = newChildren;
        }
    }

    @Override
    protected boolean pruneNodeForPrunedChild(Object prunedChild) {
        return true;
    }

//    @Override
//    public void appendAsString(Appendable out) throws IOException {
//
//        if (parent != null) {
//            out.append("(");
//        }
//
//        if ((children != null) && (children.length > 0)) {
//            for (int i = 0; i < children.length; ++i) {
//                if (i > 0) {
//                    out.append(' ');
//                    out.append(getExpressionOperator(i));
//                    out.append(' ');
//                }
//
//                if (children[i] == null) {
//                    out.append("null");
//                } else {
//                    ((org.apache.cayenne.exp.parser.SimpleNode) children[i]).appendAsString(out);
//                }
//            }
//        }
//
//        if (parent != null) {
//            out.append(')');
//        }
//    }



    public void jjtOpen() {
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) {
        parent = n;
    }

    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    protected Node wrapChild(Object child) {
        // when child is null, there's no way of telling whether this is a scalar or not... fuzzy...
        // maybe we should stop using this method - it is too generic
        return (child instanceof Node || child == null) ? (Node) child : new ASTScalar(child);
    }

    /**
     * Sets the parent to this for all children.
     */
    protected void connectChildren() {
        if (children != null) {
            for (Node child : children) {
                // although nulls are expected to be wrapped in scalar,
                // still doing a check here to make it more robust
                if (child != null) {
                    child.jjtSetParent(this);
                }
            }
        }
    }
}

/* JavaCC - OriginalChecksum=573077ad8cf1bdfc9a7547f811a10503 (do not edit this line) */
