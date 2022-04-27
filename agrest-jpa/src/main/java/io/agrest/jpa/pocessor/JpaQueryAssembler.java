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
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaQueryAssembler implements IJpaQueryAssembler {

    private final IAgJpaPersister persister;
    private final IJpaExpParser expParser;

    public JpaQueryAssembler(@Inject IAgJpaPersister persister,
                             @Inject IJpaExpParser expParser) {
        this.persister = persister;
        this.expParser = expParser;
    }

    @Override
    public <T> JpaQueryBuilder createRootQuery(SelectContext<T> context) {
        RootResourceEntity<T> entity = context.getEntity();
        JpaQueryBuilder query = context.getId() != null
                ? createRootIdQuery(entity, context.getId().asMap(entity.getAgEntity()))
                : createBaseQuery(entity);

        JpaExpression parsedExp = expParser.parse(entity.getExp());
        query.where(parsedExp);

        EntityParent<?> parent = context.getParent();
        if (parent != null) {
            query.where(parentQualifier(parent));
        }

        return query;
    }

    @Override
    public <T> JpaQueryBuilder createQueryWithParentQualifier(NestedResourceEntity<T> entity) {
        String relationship = entity.getIncoming().getName();
        JpaQueryBuilder parentSelect = JpaProcessor.getEntity(entity.getParent()).getSelect();

        JpaQueryBuilder select;
        if(entity.getIncoming().isToMany()) {
            select = JpaQueryBuilder.select("r").selectSpec("e.id")
                    .from(entity.getParent().getName() + " e")
                    .from(", IN (e." + relationship + ") r");
        } else {
            select = JpaQueryBuilder.select("e." + relationship).selectSpec("e.id")
                    .from(entity.getParent().getName() + " e");
        }
        if(parentSelect.hasWhere()) {
            // TODO: translate to a new root
            select.where(parentSelect.getWhere());
        }
        return select;
    }

    @Override
    public <T, P> JpaQueryBuilder createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentIt) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected <T> JpaQueryBuilder createRootIdQuery(RootResourceEntity<T> entity, Map<String, Object> idMap) {
        return createBaseQuery(entity)
                .where(createIdQualifier(entity.getAgEntity(), idMap, "e"));
    }

    @Override
    public JpaQueryBuilder createByIdQuery(AgEntity<?> entity, Map<String, Object> idMap) {
        return JpaQueryBuilder.select("e")
                .from(entity.getName() + " e")
                .where(createIdQualifier(entity, idMap, "e"));
    }

    JpaExpression createIdQualifier(AgEntity<?> entity, Map<String, Object> idMap, String alias) {
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

    private JpaExpression parentQualifier(EntityParent<?> parent) {

        return new JpaExpression("");
    }

    protected <T> JpaQueryBuilder createBaseQuery(ResourceEntity<T> entity) {
        JpaQueryBuilder query = JpaQueryBuilder.select("e").from(entity.getName() + " e");

        if (!entity.isFiltered()) {
            int limit = entity.getLimit();
            if (limit > 0) {
                query.limit(limit);
            }
        }

        for (Sort o : entity.getOrderings()) {
            query.orderBy(toOrdering(entity, o));
        }

        return query;
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
