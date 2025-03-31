package io.agrest.exp.parser;

import java.util.Objects;

public abstract class ExpBaseScalar<T> extends SimpleNode {

    public ExpBaseScalar(int i) {
        super(i);
    }

    public ExpBaseScalar(AgExpressionParser p, int i) {
        super(p, i);
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        return (T)jjtGetValue();
    }

    public void setValue(T value) {
        jjtSetValue(value);
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

        ExpBaseScalar<?> expOther = (ExpBaseScalar<?>) o;
        return Objects.equals(getValue(), expOther.getValue());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(getValue());
    }
}
