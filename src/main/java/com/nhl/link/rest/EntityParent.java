package com.nhl.link.rest;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a parent in a relationship request.
 * 
 * @since 1.4
 */
public class EntityParent<P> {

	private Class<P> type;
	private Object id;
	private String relationship;

	public EntityParent(Class<P> parentType, Object parentId, String relationshipFromParent) {

		if (parentType == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent type is missing");
		}

		if (parentId == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent ID is missing");
		}

		if (relationshipFromParent == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Related parent relationship is missing");
		}

		this.id = parentId;
		this.type = parentType;
		this.relationship = relationshipFromParent;
	}

	public Class<P> getType() {
		return type;
	}

	public Object getId() {
		return id;
	}

	public String getRelationship() {
		return relationship;
	}

	public Expression qualifier(EntityResolver resolver) {

		ObjEntity parentEntity = resolver.getObjEntity(type);
		ObjRelationship objRelationship = parentEntity.getRelationship(relationship);

		if (objRelationship == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid relationship: '" + relationship + "'");
		}

		// navigate through DbRelationships ... there may be no reverse
		// ObjRel.. Reverse DB should always be there

		if (id instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> parentIds = (Map<String, Object>) id;
			List<Expression> expressions = new ArrayList<>();
			for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
				DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
				for (DbJoin join : reverseRelationship.getJoins()) {
					expressions.add(ExpressionFactory.matchDbExp(join.getSourceName(), parentIds.get(join.getTargetName())));
				}
			}
			return ExpressionFactory.and(expressions);
		} else {
			return ExpressionFactory.matchDbExp(objRelationship.getReverseDbRelationshipPath(), id);
		}
	}
}
