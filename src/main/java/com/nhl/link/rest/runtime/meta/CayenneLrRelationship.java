package com.nhl.link.rest.runtime.meta;

import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;

/**
 * @since 1.12
 */
class CayenneLrRelationship implements LrRelationship {

	private ObjRelationship objRelationship;
	private Class<?> targetEntityType;
	private LrDataMap dataMap;

	CayenneLrRelationship(ObjRelationship objRelationship, Class<?> targetEntityType, LrDataMap dataMap) {
		this.objRelationship = objRelationship;
		this.targetEntityType = targetEntityType;
		this.dataMap = dataMap;
	}

	@Override
	public String getName() {
		return objRelationship.getName();
	}

	@Override
	public LrEntity<?> getTargetEntity() {
		// potentially deferred resolution of target entity from DataMap
		return dataMap.getEntity(targetEntityType);
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
