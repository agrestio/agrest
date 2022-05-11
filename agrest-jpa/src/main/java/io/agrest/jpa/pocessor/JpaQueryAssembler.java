package io.agrest.jpa.pocessor;

import java.util.Iterator;
import java.util.Map;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.exp.JpaExpression;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

/**
 * @since 5.0
 */
public class JpaQueryAssembler implements IJpaQueryAssembler {

    private final IAgJpaPersister persister;
    private final IJpaExpParser expParser;

    private final Provider<AgDataMap> dataMap;

    public JpaQueryAssembler(@Inject IAgJpaPersister persister,
                             @Inject IJpaExpParser expParser,
                             @Inject Provider<AgDataMap> dataMap) {
        this.persister = persister;
        this.expParser = expParser;
        this.dataMap = dataMap;
    }

    @Override
    public <T> JpaQueryBuilder createRootQuery(SelectContext<T> context) {
        RootResourceEntity<T> entity = context.getEntity();
        EntityParent<?> parent = context.getParent();

        JpaQueryBuilder query;
        if(parent == null) {
            query = context.getId() != null
                    ? createRootIdQuery(entity, context.getId())
                    : createBaseQuery(entity);
        } else {
            AgEntity<?> agEntity = dataMap.get().getEntity(parent.getType());
            AgRelationship incomingRelationship = agEntity.getRelationship(parent.getRelationship());
            if (incomingRelationship == null) {
                throw AgException.internalServerError("Invalid parent relationship: '%s'", parent.getRelationship());
            }
            query = viaParentJoinQuery(agEntity.getName(), parent.getRelationship(), incomingRelationship.isToMany())
                    .where(createIdQualifier(parent.getId().asMap(agEntity), "e"));
        }

        JpaExpression parsedExp = expParser.parse(entity.getExp());
        query.where(parsedExp);
        applyLimitAndOrdering(entity, query);

        return query;
    }

    @Override
    public <T> JpaQueryBuilder createQueryWithParentQualifier(NestedResourceEntity<T> entity) {
        String relationship = entity.getIncoming().getName();
        JpaQueryBuilder parentSelect = JpaProcessor.getEntity(entity.getParent()).getSelect();

        JpaQueryBuilder select = viaParentJoinQuery(entity.getParent().getName(), relationship, entity.getIncoming().isToMany())
                .selectSpec("e.id");
        if(parentSelect.hasWhere()) {
            // TODO: translate to a new root
            select.where(parentSelect.getWhere());
        }

        applyLimitAndOrdering(entity, select);
        return select;
    }

    private <T> JpaQueryBuilder viaParentJoinQuery(String parentName, String relationship, boolean toMany) {
        if(toMany) {
            return JpaQueryBuilder.select("r")
                    .from(parentName + " e")
                    .from(", IN (e." + relationship + ") r");
        } else {
            return JpaQueryBuilder.select("e." + relationship)
                    .from(parentName + " e");
        }
    }

    @Override
    public <T, P> JpaQueryBuilder createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentIt) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected <T> JpaQueryBuilder createRootIdQuery(RootResourceEntity<T> entity, AgObjectId id) {
        return createBaseQuery(entity)
                .where(createIdQualifier(id.asMap(entity.getAgEntity()), "e"));
    }


    @Override
    public JpaQueryBuilder createByIdQuery(AgEntity<?> entity, AgObjectId id) {
        return createByIdQuery(entity, id.asMap(entity));
    }

    @Override
    public JpaQueryBuilder createByIdQuery(AgEntity<?> entity, Map<String, Object> idMap) {
        return JpaQueryBuilder.select("e")
                .from(entity.getName() + " e")
                .where(createIdQualifier(idMap, "e"));
    }

    JpaExpression createIdQualifier(Map<String, Object> idMap, String alias) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(String key : idMap.keySet()) {
            if(sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append(alias).append('.').append(key).append(" = ?").append(i++);
        }
        JpaExpression expression = new JpaExpression(sb.toString());
        for(Object value: idMap.values()) {
            expression.addParameter(value);
        }
        return expression;
    }

    protected <T> JpaQueryBuilder createBaseQuery(ResourceEntity<T> entity) {
        return JpaQueryBuilder.select("e").from(entity.getName() + " e");
    }

    private <T> void applyLimitAndOrdering(ResourceEntity<T> entity, JpaQueryBuilder query) {
        if (!entity.isFiltered()) {
            int limit = entity.getLimit();
            if (limit > 0) {
                query.limit(limit);
            }
        }

        for (Sort o : entity.getOrderings()) {
            query.orderBy(toOrdering(entity, o));
        }
    }

    private <T> String toOrdering(ResourceEntity<T> entity, Sort o) {
        if(!entity.getAttributes().containsKey(o.getProperty())) {
            if(entity.getAgEntity().getIdPart(o.getProperty()) == null) {
                throw AgException.badRequest("Invalid path '%s' for '%s'", o.getProperty(), entity.getName());
            }
        }
        return o.getProperty() + " " + o.getDirection();
    }
}
