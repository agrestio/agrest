package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.ChildResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.cayenne.CayenneAgAttribute;
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

        if (context.getParent() != null && context.getEntity().getName().equals(entity.getName())) {
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
            for (ChildResourceEntity<?> c : entity.getMapBy().getChildren().values()) {
                buildChildQuery(context, entity, c);
            }
        }

        for (ChildResourceEntity<?> c : entity.getChildren().values()) {
            buildChildQuery(context, entity, c);
        }

        return query;
    }


    private void buildChildQuery(SelectContext context, ResourceEntity<?> parent, ChildResourceEntity child) {

        // if related entity is not managed by Cayenne
        if (entityResolver.getObjEntity(child.getType()) == null) {
            return;
        }

        List<Property> properties = new ArrayList<>();
        properties.add(Property.createSelf(child.getType()));

        AgRelationship relationship = child.getIncoming();
        if (relationship instanceof CayenneAgRelationship) {

            CayenneAgRelationship rel = (CayenneAgRelationship) relationship;

            for (AgAttribute attribute : parent.getAgEntity().getIds()) {

                CayenneAgAttribute cayenneAgAttribute = (CayenneAgAttribute) attribute;
                Expression propertyExp = ExpressionFactory.dbPathExp(rel.getReverseDbPath()
                        + "."
                        + cayenneAgAttribute.getDbAttribute().getName());
                properties.add(Property.create(propertyExp, (Class) attribute.getType()));
            }

            // translate expression from parent
            if (parent.getSelect().getQualifier() != null) {
                // TODO: dirty - altering ResourceEntity "qualifier" parameter during query assembly stage. This stage should
                //  do what it says it does - assembling query..
                child.andQualifier(rel.translateExpressionToSource(parent.getSelect().getQualifier()));
            }

        }

        SelectQuery childQuery = buildQuery(context, child, null);
        childQuery.setColumns(properties);
    }

    <T> SelectQuery<T> basicSelect(ResourceEntity<T> resourceEntity, AgObjectId rootId) {

        // selecting by ID overrides any explicit SelectQuery...
        if (rootId != null) {

            SelectQuery<T> query = new SelectQuery<>(resourceEntity.getType());
            query.andQualifier(buildIdQualifer(resourceEntity.getAgEntity(), rootId));
            return query;
        }

        return resourceEntity.getSelect() != null ? resourceEntity.getSelect() : new SelectQuery<>(resourceEntity.getType());
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
            if (idAttribute instanceof CayenneAgAttribute) {
                qualifiers.add(ExpressionFactory.matchDbExp(
                        ((CayenneAgAttribute) idAttribute).getDbAttribute().getName(), idValue));
            } else {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR,
                        "ID attribute '" + idAttribute.getName() + "' has no mapping to a column name");
            }
        }
        return ExpressionFactory.and(qualifiers);
    }
}
