package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

import com.nhl.link.rest.meta.LrPersistentAttribute;

/**
 * @since 1.12
 */
class CayenneLrAttribute implements LrPersistentAttribute {

	private ObjAttribute objAttribute;

	CayenneLrAttribute(ObjAttribute objAttribute) {
		this.objAttribute = objAttribute;
	}

	@Override
	public String getName() {
		return objAttribute.getName();
	}

	@Override
	public ObjAttribute getObjAttribute() {
		return objAttribute;
	}

	@Override
	public String getJavaType() {
		return objAttribute.getType();
	}

	@Override
	public int getJdbcType() {
		// TODO: this check won't be needed as soon as we switch POJOs to
		// LrDataMap instead of Cayenne DataMap. Until then we have to be
		// dealing with POJOs mapped vai CayenneLr* model.

		return objAttribute.getDbAttribute() != null ? objAttribute.getDbAttribute().getType() : Integer.MIN_VALUE;
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", objAttribute.getName());
		return tsb.toString();
	}
}
