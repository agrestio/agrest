package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;

import java.util.Objects;

public abstract class ExpGenericScalar<T> extends AgExpression {

    public ExpGenericScalar(int i) {
        super(i);
    }

    public ExpGenericScalar(AgExpressionParser p, int i) {
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

        ExpGenericScalar<?> expOther = (ExpGenericScalar<?>) o;
        return Objects.equals(getValue(), expOther.getValue());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(getValue());
    }
}
