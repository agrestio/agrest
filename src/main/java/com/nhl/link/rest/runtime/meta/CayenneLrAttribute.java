package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjAttribute;

import com.nhl.link.rest.meta.LrAttribute;

/**
 * @since 1.12
 */
class CayenneLrAttribute implements LrAttribute {
	
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
		return objAttribute.getDbAttribute().getType();
	}
}
