package io.agrest.cayenne.processor;

import io.agrest.AgException;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathOps;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.runtime.EntityParent;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CayenneUtil {

    private CayenneUtil() {
    }

    /**
     * @since 5.0
     */
    public static ObjectId toObjectId(
            IPathResolver pathResolver,
            ObjectContext context,
            AgEntity<?> agEntity,
            Object idValueOrMap) {

        if (idValueOrMap == null) {
            throw AgException.badRequest("No id specified");
        }

        ObjEntity entity = context.getEntityResolver().getObjEntity(agEntity.getType());
        if (entity == null) {
            throw AgException.internalServerError("Unknown entity class: %s", agEntity.getType());
        }

        Collection<ObjAttribute> pks = entity.getPrimaryKeys();
        if (pks.size() == 1) {
            ObjAttribute pk = pks.iterator().next();
            return ObjectId.of(entity.getName(), pk.getDbAttributeName(), idValueOrMap);
        } else {

            if (!(idValueOrMap instanceof Map)) {
                throw AgException.internalServerError("Expected a map of id values for entity: %s", agEntity.getName());
            }

            Map<String, ?> idMap = (Map<String, ?>) idValueOrMap;
            Map<String, Object> normalizedIdMap = new HashMap<>(pks.size() * 2);
            idMap.forEach((k, v) -> {

                ASTPath kp = pathResolver.resolve(agEntity.getName(), k).getPathExp();
                if (kp instanceof ASTDbPath) {
                    normalizedIdMap.put(kp.getPath(), v);
                } else {
                    ObjAttribute pk = entity.getAttribute(kp.getPath());
                    if (pk == null) {
                        throw AgException.internalServerError("No pk attribute %s.%s", entity.getName(), kp.getPath());
                    }

                    normalizedIdMap.put(pk.getDbAttributeName(), v);
                }
            });

            return ObjectId.of(entity.getName(), normalizedIdMap);
        }
    }


    // TODO: this logic is somewhat duplicated in CayenneQueryAssembler.createQueryForIds. Maybe it belongs there to begin with?
    public static <A> A findById(
            IPathResolver pathResolver,
            ObjectContext context,
            AgEntity<A> agEntity,
            AgObjectId id) {

        ObjEntity entity = context.getEntityResolver().getObjEntity(agEntity.getType());

        // sanity checking...
        if (entity == null) {
            throw AgException.internalServerError("Unknown entity class: %s", agEntity.getType());
        }

        if (id == null) {
            throw AgException.badRequest("No id specified");
        }

        ObjectSelect<A> query = ObjectSelect.query(agEntity.getType());
        for (AgIdPart idPart : agEntity.getIdParts()) {
            ASTPath idPath = pathResolver.resolve(agEntity.getName(), idPart.getName()).getPathExp();
            query.and(ExpressionFactory.matchExp(idPath, id.get(idPart.getName())));
        }

        return query.selectOne(context);
    }

    public static Expression parentQualifier(
            IPathResolver pathResolver,
            AgEntity<?> parentAgEntity,
            EntityParent<?> parent,
            EntityResolver resolver) {

        ObjEntity parentObjEntity = resolver.getObjEntity(parent.getType());

        ObjRelationship objRelationship = parentObjEntity.getRelationship(parent.getRelationship());
        if (objRelationship == null) {
            throw AgException.internalServerError("Invalid parent relationship: '%s'", parent.getRelationship());
        }

        ASTDbPath reversePath = new ASTDbPath(objRelationship.getReverseDbRelationshipPath());

        // Navigate through DbRelationships. There may be no reverse ObjRelationship. Reverse DB should always be there

        AgObjectId id = parent.getId();
        Function<AgIdPart, Expression> expBuilder = p -> {
            ASTPath idPartPath = pathResolver.resolve(parentObjEntity.getName(), p.getName()).getPathExp();
            Expression pathExp = PathOps.concatWithDbPath(parentObjEntity, reversePath, idPartPath);

            Object val = id.get(p.getName());

            if (val == null) {
                throw AgException.badRequest(
                        "Failed to build Cayenne qualifier for a by-parent relationship '%s' - one of the parent ID parts is missing: %s",
                        parent.getRelationship(),
                        p.getName());
            }

            return ExpressionFactory.matchExp(pathExp, val);
        };

        if (id.size() == 1) {
            return expBuilder.apply(parentAgEntity.getIdParts().iterator().next());
        }

        List<Expression> expressions = new ArrayList<>(id.size());
        for (AgIdPart idPart : parentAgEntity.getIdParts()) {
            expressions.add(expBuilder.apply(idPart));
        }

        return ExpressionFactory.and(expressions);
    }
}
