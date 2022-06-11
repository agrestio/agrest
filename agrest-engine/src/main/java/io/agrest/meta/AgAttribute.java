package io.agrest.meta;

import io.agrest.reader.DataReader;

/**
 * Represents an entity "simple" property.
 *
 * @since 1.12
 */
public interface AgAttribute {

    String getName();

    /**
     * @since 1.24
     */
    Class<?> getType();

    /**
     * @since 4.7
     */
    boolean isReadable();

    /**
     * @since 4.7
     */
    boolean isWritable();

    /**
     * @since 5.0
     */
    DataReader getDataReader();
}
