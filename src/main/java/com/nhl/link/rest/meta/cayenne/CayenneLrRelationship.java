package com.nhl.link.rest.meta.cayenne;

import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.meta.LrPersistentRelationship;

/**
 * @since 1.12
 */
public class CayenneLrRelationship implements LrPersistentRelationship {

	private ObjRelationship objRelationship;
	private Class<?> targetEntityType;

	public CayenneLrRelationship(ObjRelationship objRelationship, Class<?> targetEntityType) {
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
