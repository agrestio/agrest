package io.agrest.meta;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * @since 4.1
 */
public class DefaultAgIdPart implements AgIdPart {

    private final String name;
    private final Class<?> javaType;
    private final PropertyReader reader;
    private final ASTPath path;

    public DefaultAgIdPart(String name, Class<?> javaType, PropertyReader reader, ASTPath path) {
        this.name = name;
        this.javaType = javaType;
        this.reader = reader;
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return javaType;
    }

    @Override
    public ASTPath getPathExp() {
        return path;
    }

    @Override
    public PropertyReader getReader() {
        return reader;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "[" + getName() + "]";
    }
}
