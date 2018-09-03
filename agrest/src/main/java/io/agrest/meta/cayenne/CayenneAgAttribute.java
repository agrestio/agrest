package io.agrest.meta.cayenne;

import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 1.12
 */
public class CayenneAgAttribute extends DefaultAgAttribute implements AgPersistentAttribute {

	private ObjAttribute objAttribute;

	/**
	 * @since 1.24
	 */
	public CayenneAgAttribute(ObjAttribute objAttribute, Class<?> type) {
		super(objAttribute.getName(), type);
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
