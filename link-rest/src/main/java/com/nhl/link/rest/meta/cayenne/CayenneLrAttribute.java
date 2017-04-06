package com.nhl.link.rest.meta.cayenne;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 1.12
 */
public class CayenneLrAttribute extends DefaultLrAttribute implements LrPersistentAttribute {

	private ObjAttribute objAttribute;

	/**
	 * @since 1.24
	 */
	public CayenneLrAttribute(ObjAttribute objAttribute, Class<?> type, JsonValueConverter converter) {
		super(objAttribute.getName(), type, converter);
		this.objAttribute = objAttribute;
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
