package io.agrest.meta;

import io.agrest.property.PropertyReader;

/**
 * @since 5.0
 */
public class DefaultAttribute implements AgAttribute {

    private final String name;
    private final Class<?> javaType;
    private final boolean readable;
    private final boolean writable;
    private final PropertyReader propertyReader;

    public DefaultAttribute(
            String name,
            Class<?> javaType,
            boolean readable,
            boolean writable,
            PropertyReader propertyReader) {
        this.name = name;
        this.javaType = javaType;
        this.readable = readable;
        this.writable = writable;
        this.propertyReader = propertyReader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return javaType;
    }

    /**
     * @since 4.7
     */
    @Override
    public boolean isReadable() {
        return readable;
    }

    /**
     * @since 4.7
     */
    @Override
    public boolean isWritable() {
        return writable;
    }

    /**
     * @since 2.10
     */
    @Override
    public PropertyReader getPropertyReader() {
        return propertyReader;
    }

    @Override
    public String toString() {
        return "DefaultAgAttribute[" + getName() + "]";
    }
}
