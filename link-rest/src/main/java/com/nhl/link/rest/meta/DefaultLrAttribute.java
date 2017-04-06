package com.nhl.link.rest.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 1.12
 */
public class DefaultLrAttribute implements LrAttribute {

	private String name;
	private Class<?> javaType;
	private JsonValueConverter converter;

	public DefaultLrAttribute(String name, Class<?> javaType, JsonValueConverter converter) {
		this.name = name;
		this.javaType = javaType;
		this.converter = converter;
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
	public Object extractValue(JsonNode node) {
		return converter.value(node);
	}

	@Override
	public Class<?> getType() {
		return javaType;
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", name);
		return tsb.toString();
	}
}
