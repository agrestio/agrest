package com.nhl.link.rest.meta.cayenne;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrJoin;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjRelationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @since 1.12
 */
public class CayenneLrRelationship implements LrPersistentRelationship {

	private ObjRelationship objRelationship;
	private LrEntity<?> targetEntity;
	private Collection<LrJoin> joins;

	public CayenneLrRelationship(ObjRelationship objRelationship, LrEntity<?> targetEntity) {
		this.objRelationship = objRelationship;
		this.targetEntity = Objects.requireNonNull(targetEntity);
		this.joins = collectJoins(objRelationship);
	}

	private Collection<LrJoin> collectJoins(ObjRelationship objRelationship) {
		Collection<LrJoin> joins = new ArrayList<>();
		for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
			DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
			for (DbJoin join : reverseRelationship.getJoins()) {
				joins.add(new LrJoin() {
					@Override
					public String getSourceColumnName() {
						return join.getTargetName();
					}

					@Override
					public String getTargetColumnName() {
						return join.getSourceName();
					}
				});
			}
		}
		return joins;
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
	public boolean isToDependentEntity() {
		return getDbRelationship().isToDependentPK();
	}

	@Override
	public boolean isPrimaryKey() {
		return getDbRelationship().getReverseRelationship().isToDependentPK();
	}

	@Override
	public Collection<LrJoin> getJoins() {
		return joins;
	}

	private DbRelationship getDbRelationship() {
		return objRelationship.getDbRelationships().get(0);
	}
}
