package io.agrest.exp;

import io.agrest.protocol.Exp;

import java.util.Objects;

/**
 * @since 4.4
 * @deprecated in favor of the new unified expression API
 */
@Deprecated(since = "5.0")
public class SimpleExp implements Exp {

    private final String template;

    public SimpleExp(String template) {
        this.template = Objects.requireNonNull(template);
    }

    @Override
    public void visit(ExpVisitor visitor) {
        visitor.visitSimpleExp(this);
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleExp simpleExp = (SimpleExp) o;
        return template.equals(simpleExp.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template);
    }

    @Override
    public String toString() {
        return "exp " + template;
    }
}
