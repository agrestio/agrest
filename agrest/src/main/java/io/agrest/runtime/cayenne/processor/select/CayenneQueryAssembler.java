package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.cayenne.CayenneAgAttribute;
import io.agrest.meta.cayenne.CayenneAgRelationship;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @since 3.4
 */
public class CayenneQueryAssembler {

    private EntityResolver cayenneEntityResolver;

    public CayenneQueryAssembler(EntityResolver cayenneEntityResolver) {
        this.cayenneEntityResolver = cayenneEntityResolver;
    }

    public <T> SelectQuery<T> createRootQuery(SelectContext<T> context) {

        SelectQuery<T> query = context.getId() != null
                ? createRootIdQuery(context.getEntity(), context.getId())
                : createQuery(context.getEntity());

        if (context.getParent() != null) {
            query.andQualifier(context.getParent().qualifier(cayenneEntityResolver));
        }

        return query;
    }

    public <T> SelectQuery<T> createQueryWithParentQualifier(NestedResourceEntity<T> entity) {

        SelectQuery<T> query = createQuery(entity);

        // TODO: this check prevents us from using overlaid CayenneAgRelationships
        if (!(entity.getIncoming() instanceof CayenneAgRelationship)) {
            return query;
        }

        CayenneAgRelationship relationship = (CayenneAgRelationship) entity.getIncoming();
        String reversePath = relationship.getReverseDbPath();

        List<Property<?>> properties = new ArrayList<>();
        properties.add(Property.createSelf(entity.getType()));

        for (AgAttribute attribute : entity.getParent().getAgEntity().getIds()) {

            // this works as
            CayenneAgAttribute cayenneAgAttribute = (CayenneAgAttribute) attribute;
            Expression propertyExp = ExpressionFactory.dbPathExp(reversePath
                    + "."
                    + cayenneAgAttribute.getDbAttribute().getName());
            properties.add(Property.create(propertyExp, (Class) attribute.getType()));
        }

        query.setColumns(properties);

        // translate expression from parent
        Expression parentQualifier = entity.getParent().getSelect().getQualifier();
        if (parentQualifier != null) {
            query.andQualifier(relationship.translateExpressionToSource(parentQualifier));
        }

        return query;
    }

    public <T, P> SelectQuery<T> createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentData) {

        SelectQuery<T> query = createQuery(entity);

        // TODO: this check prevents us from using overlaid CayenneAgRelationships
        if (!(entity.getIncoming() instanceof CayenneAgRelationship)) {
            return query;
        }

        CayenneAgRelationship relationship = (CayenneAgRelationship) entity.getIncoming();
        String outgoingPath = relationship.getReverseDbPath();

        List<Property<?>> properties = new ArrayList<>();
        properties.add(Property.createSelf(entity.getType()));

        for (AgAttribute attribute : entity.getParent().getAgEntity().getIds()) {

            CayenneAgAttribute cayenneAgAttribute = (CayenneAgAttribute) attribute;
            Expression propertyExp = ExpressionFactory.dbPathExp(outgoingPath
                    + "."
                    + cayenneAgAttribute.getDbAttribute().getName());
            properties.add(Property.create(propertyExp, (Class) attribute.getType()));
        }

        query.setColumns(properties);

        // build id-based qualifier
        List<Expression> qualifiers = new ArrayList<>();
        parentData.forEachRemaining(p -> qualifiers.add(ExpressionFactory.matchDbExp(outgoingPath, p)));

        // TODO: There is some functionality in Cayenne that allows to break long OR qualifiers in a series of queries.
        //  How do we use it here?
        query.andQualifier(ExpressionFactory.joinExp(Expression.OR, qualifiers));

        return query;
    }

    public <T> SelectQuery<T> createRootIdQuery(ResourceEntity<T> entity, AgObjectId rootId) {

        // selecting by ID overrides any explicit SelectQuery that might have been attached to the entity

        SelectQuery<T> query = new SelectQuery<>(entity.getType());
        query.andQualifier(buildIdQualifer(entity.getAgEntity(), rootId));
        return query;
    }

    public <T> SelectQuery<T> createQuery(ResourceEntity<T> entity) {

        SelectQuery<T> query = entity.getSelect() != null
                ? entity.getSelect()
                : new SelectQuery<>(entity.getType());

        if (!entity.isFiltered()) {
            int limit = entity.getFetchLimit();
            if (limit > 0) {
                query.setPageSize(limit);
            }
        }

        if (entity.getQualifier() != null) {
            query.andQualifier(entity.getQualifier());
        }

        for (Ordering o : entity.getOrderings()) {
            query.addOrdering(o);
        }

        return query;
    }

    public Expression buildIdQualifer(AgEntity<?> entity, AgObjectId id) {

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
