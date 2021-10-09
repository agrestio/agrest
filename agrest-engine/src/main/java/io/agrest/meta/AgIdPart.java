package io.agrest.meta;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * Represents one a possibly multiple values in an entity id.
 *
 * @since 4.1
 */
public interface AgIdPart {

    String getName();

    Class<?> getType();

    /**
     * @since 4.7
     */
    boolean isReadable();

    /**
     * @since 4.7
     */
    boolean isWritable();

    PropertyReader getReader();

    // TODO: Cayenne API leak..
    ASTPath getPathExp();
}
