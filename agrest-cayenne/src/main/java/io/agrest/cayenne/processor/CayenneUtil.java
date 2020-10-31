package io.agrest.cayenne.processor;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.map.*;
import org.apache.cayenne.query.ObjectSelect;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CayenneUtil {

    private CayenneUtil() {
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
            DbEntity dbEntity = entity.getDbEntity();

            ObjectSelect<A> query = ObjectSelect.query(type);
            for (Map.Entry<String, Object> entry : ids.entrySet()) {
                query.and(ExpressionFactory.matchDbExp(dbEntity.getAttribute(entry.getKey()).getName(), entry.getValue()));
            }
            return query.selectOne(context);
        } else {
            // TODO: this will break if this is am ID attribute not mapped as ObjAttribute in Cayenne
            AgIdPart attribute = agEntity.getIdParts().iterator().next();
            return ObjectSelect.query(type, new ASTEqual(attribute.getPathExp(), id)).selectOne(context);
        }
    }

    public static Expression parentQualifier(EntityParent<?> parent, EntityResolver resolver) {

        ObjEntity parentEntity = resolver.getObjEntity(parent.getType());
        ObjRelationship objRelationship = parentEntity.getRelationship(parent.getRelationship());

        if (objRelationship == null) {
            throw new AgException(Status.BAD_REQUEST, "Invalid relationship: '" + parent.getRelationship() + "'");
        }

        // navigate through DbRelationships. There may be no reverse ObjRelationship. Reverse DB should always be there

        AgObjectId id = parent.getId();
        if (id.size() > 1) {
            List<Expression> expressions = new ArrayList<>();
            for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
                DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
                for (DbJoin join : reverseRelationship.getJoins()) {
                    Object joinValue = id.get(join.getTargetName());
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
            return ExpressionFactory.matchDbExp(objRelationship.getReverseDbRelationshipPath(), id.get());
        }
    }
}
