package io.agrest.meta;

import io.agrest.reader.DataReader;

/**
 * @since 5.0
 */
public class DefaultAttribute implements AgAttribute {

    private final String name;
    private final Class<?> javaType;
    private final boolean readable;
    private final boolean writable;
    private final DataReader dataReader;

    public DefaultAttribute(
            String name,
            Class<?> javaType,
            boolean readable,
            boolean writable,
            DataReader dataReader) {
        this.name = name;
        this.javaType = javaType;
        this.readable = readable;
        this.writable = writable;
        this.dataReader = dataReader;
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
    public DataReader getDataReader() {
        return dataReader;
    }

    @Override
    public String toString() {
        return "DefaultAgAttribute[" + getName() + "]";
    }
}
