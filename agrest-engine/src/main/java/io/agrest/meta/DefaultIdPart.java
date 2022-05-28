package io.agrest.meta;

import io.agrest.property.PropertyReader;

/**
 * @since 5.0
 */
public class DefaultIdPart implements AgIdPart {

    private final String name;
    private final Class<?> javaType;
    private final boolean readable;
    private final boolean writable;
    private final PropertyReader reader;

    public DefaultIdPart(
            String name,
            Class<?> javaType,
            boolean readable,
            boolean writable,
            PropertyReader reader) {
        this.name = name;
        this.javaType = javaType;
        this.readable = readable;
        this.writable = writable;
        this.reader = reader;
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

    @Override
    public PropertyReader getReader() {
        return reader;
    }

    @Override
    public String toString() {
        return "DefaultAgIdPart[" + getName() + "]";
    }
}
