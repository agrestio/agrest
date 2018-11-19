package io.agrest.meta;

import io.agrest.property.PropertyReader;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.exp.parser.ASTPath;
import io.agrest.backend.util.ToStringBuilder;

/**
 * @since 1.12
 */
public class DefaultAgAttribute implements AgAttribute {

	private String name;
	private Class<?> javaType;
	private PropertyReader propertyReader;

	public DefaultAgAttribute(String name, Class<?> javaType) {
		this(name, javaType, null);
	}

	public DefaultAgAttribute(String name, Class<?> javaType, PropertyReader propertyReader) {
		this.name = name;
		this.javaType = javaType;
		this.propertyReader = propertyReader;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ASTPath getPathExp() {
		return new ASTObjPath(name);
	}

	@Override
	public Class<?> getType() {
		return javaType;
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

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", name);
		return tsb.toString();
	}
}
