package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.util.ToStringBuilder;

import com.nhl.link.rest.meta.LrAttribute;

/**
 * @since 1.12
 */
public class DefaultLrAttribute implements LrAttribute {

	private String name;
	private String javaType;

	public DefaultLrAttribute(String name, String javaType) {
		this.name = name;
		this.javaType = javaType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getJavaType() {
		return javaType;
	}
	
	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", name);
		return tsb.toString();
	}
}
