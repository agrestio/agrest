package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.LinkRestException;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.ObjEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static javax.ws.rs.core.Response.Status;

class IdExpressionBuilder {

    private static final int DEFAULT_CAPACITY = 10;

    static IdExpressionBuilder forEntity(ObjEntity entity) {
        return forEntity(entity, DEFAULT_CAPACITY);
    }

    static IdExpressionBuilder forEntity(ObjEntity entity, int idCount) {
        int idSize = getIdSize(entity);

        if (idSize == 0) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Entity has no primary keys: " + entity.getName());
        }

        if (idCount <= 0) {
            idCount = DEFAULT_CAPACITY;
        }
        return new IdExpressionBuilder(entity, idCount);
    }

    private static int getIdSize(ObjEntity entity) {
        return entity.getDbEntity().getPrimaryKeys().size();
    }

    private final Collection<ObjectId> ids;
    private final ObjEntity entity;

    private IdExpressionBuilder(ObjEntity entity, int capacity) {
        this.entity = entity;
        this.ids = new ArrayList<>(capacity);
    }

    IdExpressionBuilder appendId(ObjectId id) {
        if (!entity.getName().equals(id.getEntityName())) {
            throw new IllegalStateException(
                    String.format("Expected ObjectId for entity: %s. Actual: %s", entity.getName(), id.getEntityName())
            );
        }
        ids.add(id);
        return this;
    }

    Expression buildExpression() {
        int idSize = getIdSize(entity);
        boolean isSingleId = idSize == 1;

        Expression exp;
        if (isSingleId) {
            String dbPath = entity.getPrimaryKeys().iterator().next().getDbAttributePath();
            exp = ExpressionFactory.inDbExp(dbPath, collectValues(ids));
        } else {
            exp = ExpressionFactory.or(collectExpressions(ids));
        }

        return exp;
    }

    private static Collection<Object> collectValues(Collection<ObjectId> ids) {
        Collection<Object> values = new HashSet<>(ids.size());
        for (ObjectId id : ids) {
            values.add(id.getIdSnapshot().values().iterator().next());
        }
        return values;
    }

    private static Collection<Expression> collectExpressions(Collection<ObjectId> ids) {
        Collection<Expression> exps = new ArrayList<>(ids.size());
        for (ObjectId id : ids) {
            exps.add(ExpressionFactory.matchAllDbExp(id.getIdSnapshot(), Expression.EQUAL_TO));
        }
        return exps;
    }

}
