package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.Select;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QueryBuilder<T> {

    private Select<T> query;

    private Consumer<Integer> pageSizeSetter;
    private Consumer<Expression> qualifierSetter;
    private Consumer<Ordering> orderingSetter;
    private Consumer<PrefetchTreeNode> prefetchSetter;

    public QueryBuilder(SelectContext<T> context) {
        // selecting by ID overrides any explicit SelectQuery...
        if (context.isById()) {
            Class<T> root = context.getType();
            SelectQuery<T> query = new SelectQuery<>(root);
            query.andQualifier(buildIdQualifer(context.getEntity().getLrEntity(), context.getId()));
            this.query = query;

        } else if (context.getSelect() != null) {
            this.query = context.getSelect();

        } else {
            this.query = new SelectQuery<>(context.getType());
        }

        initSetters(this.query.getClass());
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

    private void updateSetters(Class<? extends Select> queryType) {
        if (query != null && query.getClass().isAssignableFrom(queryType)) {
            return;
        }
        initSetters(queryType);
    }

    private void initSetters(Class<? extends Select> queryType) {
        // important: not using method references, because query can be null
        if (SelectQuery.class.isAssignableFrom(queryType)) {
            pageSizeSetter = pageSize -> ((SelectQuery<T>) query).setPageSize(pageSize);
            qualifierSetter = exp -> ((SelectQuery<T>) query).andQualifier(exp);
            orderingSetter = ordering -> ((SelectQuery<T>) query).addOrdering(ordering);
            prefetchSetter = prefetch -> ((SelectQuery<T>) query).setPrefetchTree(prefetch);

        } else if (ObjectSelect.class.isAssignableFrom(queryType)) {
            pageSizeSetter = pageSize -> updateQuery(() -> ((ObjectSelect<T>) query).pageSize(pageSize));
            qualifierSetter = exp -> updateQuery(() -> ((ObjectSelect<T>) query).where(exp));
            orderingSetter = ordering -> updateQuery(() -> ((ObjectSelect<T>) query).orderBy(ordering));
            prefetchSetter = prefetch -> updateQuery(() -> ((ObjectSelect<T>) query).prefetch(prefetch));

        } else if (ColumnSelect.class.isAssignableFrom(queryType)) {
            pageSizeSetter = pageSize -> query = ((ColumnSelect<T>) query).pageSize(pageSize);
            qualifierSetter = exp -> query = ((ColumnSelect<T>) query).where(exp);
            orderingSetter = ordering -> query = ((ColumnSelect<T>) query).orderBy(ordering);
            prefetchSetter = prefetch -> query = ((ColumnSelect<T>) query).prefetch(prefetch);

        } else {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown query type: " + queryType.getName());
        }
    }

    private <E extends Select<T>> void updateQuery(Supplier<E> updater) {
        E updatedQuery = updater.get();
        updateSetters(updatedQuery.getClass());
        query = updatedQuery;
    }

    public QueryBuilder<T> pageSize(int pageSize) {
        pageSizeSetter.accept(pageSize);
        return this;
    }

    public QueryBuilder<T> qualifier(Expression expression) {
        qualifierSetter.accept(expression);
        return this;
    }

    public QueryBuilder<T> ordering(Ordering ordering) {
        orderingSetter.accept(ordering);
        return this;
    }

    public QueryBuilder<T> prefetch(PrefetchTreeNode prefetch) {
        prefetchSetter.accept(prefetch);
        return this;
    }

    public Select<T> buildQuery() {
        return query;
    }
}
