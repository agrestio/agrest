/* Generated By:JJTree: Do not edit this line. ExpRoot.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ExpRoot extends SimpleNode {

    private static final Object[] EMPTY_PARAMS = new Object[0];
    private Map<String, Object> namedParams;
    private Object[] positionalParams;

    public ExpRoot(int id) {
        super(id);
    }

    public ExpRoot(AgExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }

    public ExpRoot namedParams(Map<String, Object> params) {
        namedParams = params;
        return this;
    }

    public ExpRoot positionalParams(Object... params) {
        positionalParams = params;
        return this;
    }

    public boolean hasNamedParams() {
        return namedParams != null && !namedParams.isEmpty();
    }

    public Map<String, Object> getNamedParams() {
        return hasNamedParams()
                ? namedParams
                : Collections.emptyMap();
    }

    public boolean hasPositionalParams() {
        return positionalParams != null && positionalParams.length > 0;
    }

    public Object[] getPositionalParams() {
        return hasPositionalParams()
                ? positionalParams
                : EMPTY_PARAMS;
    }

    public String getTemplate() {
        return (String)jjtGetValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExpRoot expRoot = (ExpRoot) o;
        return Objects.equals(getNamedParams(), expRoot.getNamedParams())
                && Arrays.equals(getPositionalParams(), expRoot.getPositionalParams());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), getNamedParams());
        result = 31 * result + Arrays.hashCode(getPositionalParams());
        return result;
    }
}
/* JavaCC - OriginalChecksum=4a9089c324a115a2f31fc5d599cc062f (do not edit this line) */
