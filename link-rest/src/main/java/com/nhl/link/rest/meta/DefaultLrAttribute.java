package com.nhl.link.rest.meta;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.util.ToStringBuilder;

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
	public ASTPath getPathExp() {
		return new ASTObjPath(name);
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
