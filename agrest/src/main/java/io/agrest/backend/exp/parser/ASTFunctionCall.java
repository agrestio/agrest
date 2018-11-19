package io.agrest.backend.exp.parser;

import io.agrest.backend.exp.Expression;


/**
 *
 *
 */
public abstract class ASTFunctionCall extends SimpleNode {

    private String functionName;

    ASTFunctionCall(int id, String functionName) {
        super(id);
        this.functionName = functionName;
    }

    public ASTFunctionCall(int id, String functionName, Object... nodes) {
        this(id, functionName);
        this.functionName = functionName;
        int len = nodes.length;
        for (int i = 0; i < len; i++) {
            jjtAddChild(wrapChild(nodes[i]), i);
        }

        connectChildren();
    }

    @Override
    public int getType() {
        return Expression.FUNCTION_CALL;
    }

    public boolean needParenthesis() {
        return true;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ASTFunctionCall that = (ASTFunctionCall) o;
        return functionName.equals(that.functionName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + functionName.hashCode();
    }

    /**
     *
     * @param functionName in UPPER_UNDERSCORE convention
     * @return functionName in camelCase convention
     */
    protected static String nameToCamelCase(String functionName) {
        String[] parts = functionName.split("_");
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(String part : parts) {
            if(first) {
                sb.append(part.toLowerCase());
                first = false;
            } else {
                char[] chars = part.toLowerCase().toCharArray();
                chars[0] = Character.toTitleCase(chars[0]);
                sb.append(chars);
            }
        }
        return sb.toString();
    }
}