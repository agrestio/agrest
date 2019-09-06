package io.agrest.meta.cayenne;

import io.agrest.meta.DefaultAgAttribute;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 3.4
 */
public class CayenneAgObjAttribute extends DefaultAgAttribute implements CayenneAgAttribute {

	private ObjAttribute objAttribute;

	public CayenneAgObjAttribute(ObjAttribute objAttribute, Class<?> type, PropertyReader propertyReader) {
		super(objAttribute.getName(), type, propertyReader);
		this.objAttribute = objAttribute;
	}

	public ObjAttribute getObjAttribute() {
		return objAttribute;
	}

	@Override
	public DbAttribute getDbAttribute() {
		return objAttribute.getDbAttribute();
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", objAttribute.getName());
		return tsb.toString();
	}
}
