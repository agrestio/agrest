package io.agrest.exp;

import java.util.Map;
import java.util.Objects;

/**
 * @since 4.4
 */
public class NamedParamsExp extends SimpleExp {

    private final Map<String, Object> params;

    public NamedParamsExp(String exp, Map<String, Object> params) {
        super(exp);
        this.params = Objects.requireNonNull(params);
    }

    @Override
    public void visit(ExpVisitor visitor) {
        visitor.visitNamedParamsExp(this);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NamedParamsExp that = (NamedParamsExp) o;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), params);
    }
}
