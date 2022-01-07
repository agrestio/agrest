package io.agrest.cayenne.processor;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.*;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CayenneUtil {

    private CayenneUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <A> A findById(
            IPathResolver pathResolver,
            ObjectContext context,
            Class<A> type,
            AgEntity<?> agEntity,
            AgObjectId id) {

        ObjEntity entity = context.getEntityResolver().getObjEntity(type);

        // sanity checking...
        if (entity == null) {
            throw AgException.internalServerError("Unknown entity class: %s", type);
        }

        if (id == null) {
            throw AgException.badRequest("No id specified");
        }

        Object idVal = id.get();

        if (idVal instanceof Map) {
            Map<String, Object> ids = (Map<String, Object>) idVal;

            ObjectSelect<A> query = ObjectSelect.query(type);
            for (Map.Entry<String, Object> entry : ids.entrySet()) {

                ASTPath idPath = pathResolver.resolve(agEntity, entity.getName()).getPathExp();
                query.and(ExpressionFactory.matchExp(idPath, entry.getValue()));
            }
            return query.selectOne(context);
        } else {
            AgIdPart idPart = agEntity.getIdParts().iterator().next();
            ASTPath idPath = pathResolver.resolve(agEntity, idPart.getName()).getPathExp();
            return ObjectSelect.query(type).where(new ASTEqual(idPath, idVal)).selectOne(context);
        }
    }

    public static Expression parentQualifier(IPathResolver pathResolver, EntityParent<?> parent, EntityResolver resolver) {

        ObjEntity parentEntity = resolver.getObjEntity(parent.getType());
        ObjRelationship objRelationship = parentEntity.getRelationship(parent.getRelationship());

        if (objRelationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", parent.getRelationship());
        }

        // navigate through DbRelationships. There may be no reverse ObjRelationship. Reverse DB should always be there

        AgObjectId id = parent.getId();
        if (id.size() > 1) {
            List<Expression> expressions = new ArrayList<>();
            for (DbRelationship dbRelationship : objRelationship.getDbRelationships()) {
                DbRelationship reverseRelationship = dbRelationship.getReverseRelationship();
                for (DbJoin join : reverseRelationship.getJoins()) {

                    // TODO: #521 ... Must use resolver to build the expression
                    Object joinValue = id.get(join.getTargetName());
                    if (joinValue == null) {
                        throw AgException.badRequest(
                                "Failed to build Cayenne qualifier for a by-parent relationship '%s' - one of the parent ID parts is missing: %s",
                                parent.getRelationship(),
                                join.getTargetName());
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
