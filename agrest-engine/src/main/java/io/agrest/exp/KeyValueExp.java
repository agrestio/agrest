package io.agrest.exp;

import io.agrest.protocol.Exp;

/**
 * @since 5.0
 * @deprecated in favor of the new unified expression API
 */
@Deprecated(since = "5.0")
public class KeyValueExp implements Exp {

    private final String key;
    private final String op;
    private final Object value;

    public KeyValueExp(String key, String op, Object value) {
        this.key = key;
        this.op = op;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getOp() {
        return op;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public void visit(ExpVisitor visitor) {
        visitor.visitKeyValueExp(this);
    }
}
