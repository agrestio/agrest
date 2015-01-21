package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.meta.LrRelationship;

/**
 * @since 1.12
 */
class CayenneLrRelationship implements LrRelationship {

	private ObjRelationship objRelationship;
	private Class<?> targetEntityType;

	CayenneLrRelationship(ObjRelationship objRelationship, Class<?> targetEntityType) {
		this.objRelationship = objRelationship;
		this.targetEntityType = targetEntityType;
	}

	@Override
	public String getName() {
		return objRelationship.getName();
	}

	@Override
	public Class<?> getTargetEntityType() {
		return targetEntityType;
	}

	@Override
	public boolean isToMany() {
		return objRelationship.isToMany();
	}

	@Override
	public ObjRelationship getObjRelationship() {
		return objRelationship;
	}
}
