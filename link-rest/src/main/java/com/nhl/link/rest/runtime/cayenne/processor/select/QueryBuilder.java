package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class QueryBuilder<T> {

    private SelectQuery<T> query;

    public QueryBuilder(SelectContext<T> context) {
        Class<T> root = context.getType();
        // use existing query or create a new one
        SelectQuery<T> query = context.getSelect();
        // selecting by ID overrides any explicit SelectQuery...
        if (query == null || context.isById()) {
            query = new SelectQuery<>(root);
            query.setColumns(new ArrayList<>());
            if (context.isById()) {
                query.setQualifier((buildIdQualifer(context.getEntity().getLrEntity(), context.getId())));
            }
        }
        this.query = query;
    }

    private Expression buildIdQualifer(LrEntity<?> entity, LrObjectId id) {
        Collection<LrAttribute> idAttributes = entity.getIds();
        if (idAttributes.size() != id.size()) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Wrong ID size: expected " + idAttributes.size() + ", got: " + id.size());
        }

        Collection<Expression> qualifiers = new ArrayList<>();
        for (LrAttribute idAttribute : idAttributes) {
            Object idValue = id.get(idAttribute.getName());
            if (idValue == null) {
                throw new LinkRestException(Status.BAD_REQUEST,
                        "Failed to build a Cayenne qualifier for entity " + entity.getName()
                                + ": one of the entity's ID parts is missing in this ID: " + idAttribute.getName());
            }
            if (idAttribute instanceof LrPersistentAttribute) {
                qualifiers.add(ExpressionFactory.matchDbExp(
                        ((LrPersistentAttribute) idAttribute).getColumnName(), idValue));
            } else {
                // can be non-persistent attribute if assembled from @LrId by LrEntityBuilder
                qualifiers.add(ExpressionFactory.matchDbExp(idAttribute.getName(), idValue));
            }
        }
        return ExpressionFactory.and(qualifiers);
    }

    public QueryBuilder<T> pageSize(int pageSize) {
        query.setPageSize(pageSize);
        return this;
    }

    public QueryBuilder<T> qualifier(Expression expression) {
        query.andQualifier(expression);
        return this;
    }

    public QueryBuilder<T> ordering(Ordering ordering) {
        query.addOrdering(ordering);
        return this;
    }

    public QueryBuilder<T> prefetch(PrefetchTreeNode prefetch) {
        query.addPrefetch(prefetch);
        return this;
    }

    public QueryBuilder<T> count() {
        column(Property.COUNT);
        return this;
    }

    public QueryBuilder<T> count(Property<?> property) {
        column(property.count());
        return this;
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder<T> avg(Property<?> property) {
        column(property.avg());
        return this;
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder<T> sum(Property<? extends Number> property) {
        column(property.sum());
        return this;
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder<T> min(Property<?> property) {
        column(property.min());
        return this;
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder<T> max(Property<?> property) {
        column(property.max());
        return this;
    }

    public QueryBuilder<T> column(Property<?> property) {
        query.getColumns().add(property);
        return this;
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder<T> includeSelf() {
        Property<?> self = Property.createSelf((Class<? super Persistent>) query.getRoot());

        List<Property<?>> columns = (List<Property<?>>) query.getColumns();
        if (columns.isEmpty()) {
            columns.add(self);
        } else {
            Property<?>[] array = columns.toArray(new Property<?>[columns.size() + 1]);
            System.arraycopy(array, 0, array, 1, columns.size());
            array[0] = self;
            query.setColumns(new ArrayList<>(Arrays.asList(array)));
        }

        return this;
    }

    public int columnCount() {
        return Objects.requireNonNull(query.getColumns()).size();
    }

    public SelectQuery<T> buildQuery() {
        return query;
    }
}
