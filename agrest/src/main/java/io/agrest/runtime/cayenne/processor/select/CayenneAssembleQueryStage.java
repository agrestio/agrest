package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.cayenne.CayenneAgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneAssembleQueryStage implements Processor<SelectContext<?>> {

    private EntityResolver entityResolver;

    public CayenneAssembleQueryStage(@Inject ICayennePersister persister) {
        this.entityResolver = persister.entityResolver();
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {

        buildQuery(context, context.getEntity(), context.getId());
    }

    <T> SelectQuery<T> buildQuery(SelectContext<T> context, ResourceEntity<T> entity, AgObjectId rootId) {

        SelectQuery<T> query = basicSelect(entity, rootId);

        if (!entity.isFiltered()) {
            int limit = entity.getFetchLimit();
            if (limit > 0) {
                query.setPageSize(limit);
            }
        }

        if (context.getParent() != null
                // TODO override equals() for AgEntity
                && context.getEntity().getAgEntity().getName().equals(entity.getAgEntity().getName())) {
            Expression qualifier = context.getParent().qualifier(entityResolver);
            query.andQualifier(qualifier);
        }

        if (entity.getQualifier() != null) {
            query.andQualifier(entity.getQualifier());
        }

        for (Ordering o : entity.getOrderings()) {
            query.addOrdering(o);
        }

        entity.setSelect(query);

        if (entity.getMapBy() != null) {
            buildChildrenQuery(context, entity, entity.getMapBy().getChildren());
        }

        buildChildrenQuery(context, entity, entity.getChildren());

        return query;
    }


    private void buildChildrenQuery(SelectContext context, ResourceEntity<?> entity, Map<String, ResourceEntity<?>> children) {
        for (Map.Entry<String, ResourceEntity<?>> e : children.entrySet()) {
            ResourceEntity child = e.getValue();
            if (!(child.getAgEntity() instanceof AgPersistentEntity)) {
                continue;
            }

            List<Property> properties = new ArrayList<>();
            properties.add(Property.createSelf(child.getType()));

            AgRelationship relationship = entity.getAgEntity().getRelationship(e.getKey());
            if (relationship instanceof CayenneAgRelationship) {
                CayenneAgRelationship rel = (CayenneAgRelationship) relationship;
                for (AgAttribute attribute : entity.getAgEntity().getIds()) {
                    properties.add(Property.create(ExpressionFactory.dbPathExp(rel.getReverseDbName() + "." + attribute.getName()), (Class) attribute.getType()));
                }
                // transfer expression from parent
                if (entity.getSelect().getQualifier() != null) {
                    // TODO: dirty - altering ResourceEntity "qualifier" parameter during query assembly stage. This stage should
                    //  do what it says it does - assembling query..
                    child.andQualifier(rel.translateExpressionToSource(entity.getSelect().getQualifier()));
                }

            }

            SelectQuery childQuery = buildQuery(context, child, null);
            childQuery.setColumns(properties);
        }
    }

    <T> SelectQuery<T> basicSelect(ResourceEntity<T> resourceEntity, AgObjectId rootId) {

        // selecting by ID overrides any explicit SelectQuery...
        if (rootId != null) {

            SelectQuery<T> query = new SelectQuery<>(resourceEntity.getAgEntity().getType());
            query.andQualifier(buildIdQualifer(resourceEntity.getAgEntity(), rootId));
            return query;
        }

        return resourceEntity.getSelect() != null ? resourceEntity.getSelect() : new SelectQuery<>(resourceEntity.getAgEntity().getType());
    }

    private Expression buildIdQualifer(AgEntity<?> entity, AgObjectId id) {

        Collection<AgAttribute> idAttributes = entity.getIds();
        if (idAttributes.size() != id.size()) {
            throw new AgException(Response.Status.BAD_REQUEST,
                    "Wrong ID size: expected " + idAttributes.size() + ", got: " + id.size());
        }

        Collection<Expression> qualifiers = new ArrayList<>();
        for (AgAttribute idAttribute : idAttributes) {
            Object idValue = id.get(idAttribute.getName());
            if (idValue == null) {
                throw new AgException(Response.Status.BAD_REQUEST,
                        "Failed to build a Cayenne qualifier for entity " + entity.getName()
                                + ": one of the entity's ID parts is missing in this ID: " + idAttribute.getName());
            }
            if (idAttribute instanceof AgPersistentAttribute) {
                qualifiers.add(ExpressionFactory.matchDbExp(
                        ((AgPersistentAttribute) idAttribute).getColumnName(), idValue));
            } else {
                // can be non-persistent attribute if assembled from @AgId by AgEntityBuilder
                qualifiers.add(ExpressionFactory.matchDbExp(idAttribute.getName(), idValue));
            }
        }
        return ExpressionFactory.and(qualifiers);
    }
}
