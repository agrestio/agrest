package io.agrest.exp;

import java.util.Arrays;
import java.util.Objects;

/**
 * @since 4.4
 * @deprecated since 5.0 as Agrest now supports fully featured parsing for the expressions
 */
@Deprecated
public class PositionalParamsExp extends SimpleExp {

    private final Object[] params;

    public PositionalParamsExp(String exp, Object... params) {
        super(exp);
        this.params = Objects.requireNonNull(params);
    }

    @Override
    public void visit(ExpVisitor visitor) {
        visitor.visitPositionalParamsExp(this);
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PositionalParamsExp that = (PositionalParamsExp) o;
        return Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}
