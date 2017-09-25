package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.AggregationType;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.Select;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response.Status;
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
        context.setSelect(buildQuery(context));
    }

    <T> SelectQuery<T> buildQuery(SelectContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        QueryBuilder<T> query = new QueryBuilder<>(context);

        if (appendAggregateColumns(entity, query, null)) {
            appendGroupByColumns(entity, query, null);
        }

        if (!entity.isFiltered()) {
            int limit = context.getEntity().getFetchLimit();
            if (limit > 0) {
                query.pageSize(limit);
            }
        }

        if (context.getParent() != null) {
            Expression qualifier = context.getParent().qualifier(entityResolver);
            query.qualifier(qualifier);
        }

        if (entity.getQualifier() != null) {
            query.qualifier(entity.getQualifier());
        }

        for (Ordering o : entity.getOrderings()) {
            query.ordering(o);
        }

        if (!entity.getChildren().isEmpty()) {
            PrefetchTreeNode root = new PrefetchTreeNode();

            int prefetchSemantics = context.getPrefetchSemantics();
            if (prefetchSemantics <= 0) {
                // it makes more sense to use joint prefetches for single object
                // queries...
                prefetchSemantics = context.isById() && context.getId().size() == 1
                        ? PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS : PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS;
            }

            appendPrefetches(root, entity, prefetchSemantics);
            query.prefetch(root);
        }

        return query.buildQuery();
    }

    @SuppressWarnings("unchecked")
    private <T> boolean appendAggregateColumns(ResourceEntity<?> entity, QueryBuilder<T> query, Property<?> context) {
        boolean shouldAppendGroupByColumns = false;

        if (entity.isCountIncluded()) {
            shouldAppendGroupByColumns = true;
            if (context == null) {
                query.count();
            } else {
                query.count(context);
            }
        }

        if (entity.isAggregate()) {
            shouldAppendGroupByColumns = true;

            for (AggregationType aggregationType : AggregationType.values()) {
                entity.getAggregatedAttributes(aggregationType).forEach(attribute -> {
                    Property<?> property = createProperty(context, attribute.getName(), attribute.getType());
                    switch (aggregationType) {
                        case AVERAGE: {
                            query.avg(property);
                            break;
                        }
                        case SUM: {
                            query.sum(castProperty(property, Number.class));
                            break;
                        }
                        case MINIMUM: {
                            query.min(property);
                            break;
                        }
                        case MAXIMUM: {
                            query.max(property);
                            break;
                        }
                        default: {
                            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
                                    "Unsupported aggregation type: " + aggregationType.name());
                        }
                    }
                });
            }
        }

        for (Map.Entry<String, ResourceEntity<?>> e : entity.getChildren().entrySet()) {
            String relationshipName = e.getKey();
            ResourceEntity<?> child = e.getValue();
            Property<?> relationship = createProperty(context, relationshipName, child.getType());
            shouldAppendGroupByColumns = shouldAppendGroupByColumns || appendAggregateColumns(child, query, relationship);
        }

        return shouldAppendGroupByColumns;
    }

    private <T> void appendGroupByColumns(ResourceEntity<?> entity, QueryBuilder<T> query, Property<?> context) {
        entity.getAttributes().values().stream().filter(a -> !entity.isDefault(a.getName())).forEach(attribute -> {
            Property<?> property = createProperty(context, attribute.getName(), attribute.getType());
            query.column(property);
        });

        entity.getChildren().forEach((relationshipName, child) -> {
            Property<?> relationship = createProperty(context, relationshipName, child.getType());
            appendGroupByColumns(child, query, relationship);
        });
    }

    @SuppressWarnings("unchecked")
    private static Property<?> createProperty(Property<?> context, String name, Class<?> type) {
        Property<?> property = Property.create(name, (Class<? super Object>) type);
        if (context != null) {
            property = context.dot(property);
        }
        return property;
    }

    @SuppressWarnings("unchecked")
    private static <E> Property<E> castProperty(Property<?> property, Class<E> type) {
        if (!type.isAssignableFrom(property.getType())) {
            throw new LinkRestException(Status.BAD_REQUEST,
                    "Property '" + property.getName() + "' can not be cast to Property<" + type.getSimpleName() + ">");
        }
        return (Property<E>) property;
    }

    private void appendPrefetches(PrefetchTreeNode root, ResourceEntity<?> entity, int prefetchSemantics) {
        for (Map.Entry<String, ResourceEntity<?>> e : entity.getChildren().entrySet()) {

            // skip prefetches of non-persistent entities
            if (e.getValue().getLrEntity() instanceof LrPersistentEntity) {

                PrefetchTreeNode child = root.addPath(e.getKey());

                // always full prefetch related entities... we can't use phantom
                // as this will hit object cache and hence won't be cache
                // controlled via query cache anymore...
                child.setPhantom(false);
                child.setSemantics(prefetchSemantics);
                appendPrefetches(child, e.getValue(), prefetchSemantics);
            }
        }

        if (entity.getMapBy() != null) {
            appendPrefetches(root, entity.getMapBy(), prefetchSemantics);
        }
    }

    <T> SelectQuery<T> basicSelect(SelectContext<T> context) {
        return new QueryBuilder<>(context).buildQuery();
    }
}
