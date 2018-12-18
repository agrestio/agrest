package io.agrest.meta;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * @since 1.12
 */
public class CayenneAgAttribute implements AgPersistentAttribute<ASTPath> {

	private ObjAttribute objAttribute;
	private String name;
	private Class<?> javaType;
	private PropertyReader propertyReader;

	/**
	 * @since 1.24
	 */
	public CayenneAgAttribute(ObjAttribute objAttribute, Class<?> type) {
		this(objAttribute.getName(), type);
		this.objAttribute = objAttribute;
	}

	public CayenneAgAttribute(String name, Class<?> type) {
		this(name, type, null);
	}

	public CayenneAgAttribute(String name, Class<?> type, PropertyReader propertyReader) {
		this.name = name;
		this.javaType = type;
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

	@Override
	public PropertyReader getPropertyReader() {
		return propertyReader;
	}

	@Override
	public int getJdbcType() {
		return objAttribute.getDbAttribute().getType();
	}

	@Override
	public String getColumnName() {
		return objAttribute.getDbAttribute().getName();
	}

	@Override
	public boolean isMandatory() {
		return objAttribute.getDbAttribute().isMandatory();
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", objAttribute.getName());
		return tsb.toString();
	}
}
