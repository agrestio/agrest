package io.agrest.runtime.cayenne.processor;

import io.agrest.AgException;
import io.agrest.EntityParent;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.SimpleNode;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectSelect;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 1.7
 */
public final class Util {

	private Util() {
	}

	@SuppressWarnings("unchecked")
	public static <A> A findById(ObjectContext context, Class<A> type, AgEntity<?> agEntity, Object id) {
		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new AgException(Status.BAD_REQUEST, "No id specified");
		}

		if (id instanceof Map) {
			Map<String, Object> ids = (Map<String, Object>) id;
			ObjectSelect<A> query = ObjectSelect.query(type);
			for (Map.Entry<String, Object> entry : ids.entrySet()) {
				query.and(ExpressionFactory.matchDbExp(
						entity.getDbEntity().getAttribute(entry.getKey()).getName(), entry.getValue()
				));
			}
			return query.selectOne(context);
		} else {

			AgAttribute<SimpleNode> attribute = agEntity.getIds().iterator().next();
			return ObjectSelect.query(type, new ASTEqual(attribute.getPathExp(), id)).selectOne(context);
		}
	}

	public static Expression qualifierByParent(EntityParent<?> parent, EntityResolver resolver) {

		ObjEntity parentEntity = resolver.getObjEntity(parent.getType());
		ObjRelationship objRelationship = parentEntity.getRelationship(parent.getRelationship());

		if (objRelationship == null) {
			throw new AgException(Status.BAD_REQUEST, "Invalid relationship: '" + parent.getRelationship() + "'");
		}

		// navigate through DbRelationships ... there may be no reverse
		// ObjRel.. Reverse DB should always be there

		if (parent.getId().size() > 1) {
			List<Expression> expressions = new ArrayList<>();
			for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
				DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
				for (DbJoin join : reverseRelationship.getJoins()) {
					Object joinValue = parent.getId().get(join.getTargetName());
					if (joinValue == null) {
						throw new AgException(Status.BAD_REQUEST,
								"Failed to build a Cayenne qualifier for a by-parent relationship '" + parent.getRelationship() +
										"'; one of the parent's ID parts is missing in it's ID: " + join.getTargetName());
					}
					expressions.add(ExpressionFactory.matchDbExp(join.getSourceName(), joinValue));
				}
			}
			return ExpressionFactory.and(expressions);
		} else {
			return ExpressionFactory.matchDbExp(objRelationship.getReverseDbRelationshipPath(), parent.getId().get());
		}
	}


}
