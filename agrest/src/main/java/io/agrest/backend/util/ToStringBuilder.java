package io.agrest.backend.util;

import java.util.Collection;
import java.util.Map;

/**
 * A utility class to simplify implementation of Object toString methods. This
 * implementation is a trimmed version of commons-lang ToStringBuilder.
 *
 */
public class ToStringBuilder {

    protected StringBuilder buffer;
    protected Object object;
    protected int fieldCount;

    public ToStringBuilder(Object object) {
        this.object = object;
        this.buffer = new StringBuilder(128);

        appendClassName();
        appendIdentityHashCode();
        buffer.append('[');
    }

    public ToStringBuilder append(String fieldName, Object value) {

        if (fieldCount++ > 0) {
            buffer.append(',');
        }

        buffer.append(fieldName).append('=');

        if (value == null) {
            buffer.append("<null>");
        }
        else {
            appendDetail(value);
        }

        return this;
    }

    protected void appendDetail(Object value) {

        if (value instanceof Collection) {
            buffer.append(value);
        }
        else if (value instanceof Map) {
            buffer.append(value);
        }
        else if (value instanceof long[]) {
            appendArray((long[]) value);
        }
        else if (value instanceof int[]) {
            appendArray((int[]) value);
        }
        else if (value instanceof short[]) {
            appendArray((short[]) value);
        }
        else if (value instanceof byte[]) {
            appendArray((byte[]) value);
        }
        else if (value instanceof char[]) {
            appendArray((char[]) value);
        }
        else if (value instanceof double[]) {
            appendArray((double[]) value);
        }
        else if (value instanceof float[]) {
            appendArray((float[]) value);
        }
        else if (value instanceof boolean[]) {
            appendArray((boolean[]) value);
        }
        else if (value.getClass().isArray()) {
            appendArray((Object[]) value);
        }
        else {
            buffer.append(value);
        }
    }

    protected void appendArray(short[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(int[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(float[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(long[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(byte[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(double[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(char[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(boolean[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(array[i]);
        }
        buffer.append('}');
    }

    protected void appendArray(Object[] array) {
        buffer.append('{');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            appendDetail(array[i]);
        }
        buffer.append('}');
    }

    protected void appendClassName() {
        if (object != null) {
            buffer.append(object.getClass().getName());
        }
    }

    protected void appendIdentityHashCode() {
        if (object != null) {
            buffer.append('@');
            buffer.append(Integer.toHexString(System.identityHashCode(object)));
        }
    }

    /**
     * Returns a String built by the earlier invocations.
     */
    @Override
    public String toString() {
        buffer.append(']');
        return buffer.toString();
    }
}
