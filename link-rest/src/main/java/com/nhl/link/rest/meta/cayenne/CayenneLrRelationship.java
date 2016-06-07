package com.nhl.link.rest.meta.cayenne;

import com.nhl.link.rest.meta.LrEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.meta.LrPersistentRelationship;

import java.util.Objects;

/**
 * @since 1.12
 */
public class CayenneLrRelationship implements LrPersistentRelationship {

	private ObjRelationship objRelationship;
	private LrEntity<?> targetEntity;

	public CayenneLrRelationship(ObjRelationship objRelationship, LrEntity<?> targetEntity) {
		this.objRelationship = objRelationship;
		this.targetEntity = Objects.requireNonNull(targetEntity);
	}

	@Override
	public String getName() {
		return objRelationship.getName();
	}

	@Override
	public LrEntity<?> getTargetEntity() {
		return targetEntity;
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
