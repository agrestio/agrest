package io.agrest.exp.parser;

import io.agrest.AgException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ExpGenericScalar<T> extends SimpleNode {

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExpGenericScalar<?> of(Object value) {
        if (value == null) {
            return new ExpScalar(AgExpressionParserTreeConstants.JJTSCALAR);
        }

        ExpGenericScalar<?> scalar;
        if (value instanceof Collection) {
            scalar = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        } else if (value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                value = wrapPrimitiveArray(value);
            } else {
                value = Arrays.asList((Object[]) value);
            }
            scalar = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        } else {
            scalar = new ExpScalar(AgExpressionParserTreeConstants.JJTSCALAR);
        }

        ((ExpGenericScalar)scalar).setValue(value);
        return scalar;
    }

    private static List<?> wrapPrimitiveArray(Object value) {
        if(value instanceof byte[]) {
            byte[] array = (byte[]) value;
            List<Byte> result = new ArrayList<>(array.length);
            for (byte b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof short[]) {
            short[] array = (short[]) value;
            List<Short> result = new ArrayList<>(array.length);
            for (short b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof char[]) {
            char[] array = (char[]) value;
            List<Character> result = new ArrayList<>(array.length);
            for (char b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof int[]) {
            int[] array = (int[]) value;
            List<Integer> result = new ArrayList<>(array.length);
            for (int b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof long[]) {
            long[] array = (long[]) value;
            List<Long> result = new ArrayList<>(array.length);
            for (long b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof float[]) {
            float[] array = (float[]) value;
            List<Float> result = new ArrayList<>(array.length);
            for (float b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof double[]) {
            double[] array = (double[]) value;
            List<Double> result = new ArrayList<>(array.length);
            for (double b : array) {
                result.add(b);
            }
            return result;
        } else if(value instanceof boolean[]) {
            boolean[] array = (boolean[]) value;
            List<Boolean> result = new ArrayList<>(array.length);
            for (boolean b : array) {
                result.add(b);
            }
            return result;
        } else {
            throw AgException.internalServerError("Array of type '%s' is not supported as an 'in' exp parameter",
                                                  value.getClass().getComponentType().getSimpleName());
        }
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
