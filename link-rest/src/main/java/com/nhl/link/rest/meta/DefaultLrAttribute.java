package com.nhl.link.rest.meta;

import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 1.12
 */
public class DefaultLrAttribute implements LrAttribute {

	private String name;
	private Class<?> javaType;
	private PropertyReader propertyReader;

	public DefaultLrAttribute(String name, Class<?> javaType) {
		this(name, javaType, null);
	}

	public DefaultLrAttribute(String name, Class<?> javaType, PropertyReader propertyReader) {
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
