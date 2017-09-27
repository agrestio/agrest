package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.AggregationType;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.property.DataObjectPropertyReader;
import com.nhl.link.rest.property.PropertyReader;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response.Status;
import java.util.ListIterator;
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
            entity.excludeId(); // TODO: remove
            appendGroupByColumns(entity, query, null, entity.isAggregate());
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
        }

        if (entity.isAggregate()) {
            shouldAppendGroupByColumns = true;

            for (AggregationType aggregationType : AggregationType.values()) {
                ListIterator<LrAttribute> iter = entity.getAggregatedAttributes(aggregationType).listIterator();
                while (iter.hasNext()) {
                    LrAttribute attribute = iter.next();
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

                    iter.set(columnAttribute(attribute, query.columnCount() - 1));
                }
            }
        }

        for (Map.Entry<String, ResourceEntity<?>> e : entity.getAggregateChildren().entrySet()) {
            String relationshipName = e.getKey();
            ResourceEntity<?> child = e.getValue();
            Property<?> relationship = createProperty(context, relationshipName, child.getType());
            shouldAppendGroupByColumns = shouldAppendGroupByColumns || appendAggregateColumns(child, query, relationship);
        }

        return shouldAppendGroupByColumns;
    }

    @SuppressWarnings("unchecked")
    private <T> void appendGroupByColumns(ResourceEntity<?> entity, QueryBuilder<T> query, Property<?> context, boolean parentIsAggregate) {
        boolean shouldIncludeSelf = !parentIsAggregate;

        for (Map.Entry<String, LrAttribute> e : entity.getAttributes().entrySet()) {
            LrAttribute attribute = e.getValue();
            // related entity attributes will be read from the data object IF the parent is not aggregate
            if (entity.isDefault(attribute.getName())) {
                continue;
            }

            Property<?> property = createProperty(context, attribute.getName(), attribute.getType());
            query.column(property);

            e.setValue(columnAttribute(attribute, query.columnCount() - 1));
            shouldIncludeSelf = false;
        }

        // do this after all attributes have been added, because we'll add one more fictional attribute for encoding purposes
        if (entity.isCountIncluded()) {
            if (context == null) {
                query.count();
            } else {
                query.count(context);
            }
            entity.getAttributes().put("count()", columnAttribute(COUNT_ATTRIBUTE, query.columnCount() - 1));
            shouldIncludeSelf = false;
        }

        if (shouldIncludeSelf) {
            // no groupBy columns were explicitly included
            if (context == null) {
                // root entity
                query.includeSelf();
                swapAttributeReadersToSelf(entity);
                swapRelationshipReadersToSelf(entity);
            } else {
                // TODO: group by the single- or multi-column PK? or Cayenne takes care of that?
            }
        }

        // this method is called only when there is aggregation, but it's not known in which subtree, so need to track

        entity.getChildren().forEach((relationshipName, child) -> {
            Property<?> relationship = createProperty(context, relationshipName, child.getType());
            appendGroupByColumns(child, query, relationship, parentIsAggregate || entity.isAggregate());
        });

        entity.getAggregateChildren().forEach((relationshipName, child) -> {
            Property<?> relationship = createProperty(context, relationshipName, child.getType());
            appendGroupByColumns(child, query, relationship, parentIsAggregate || entity.isAggregate());
        });
    }

    private static void swapAttributeReadersToSelf(ResourceEntity<?> entity) {
        for (Map.Entry<String, LrAttribute> e : entity.getAttributes().entrySet()) {
            if (!(e.getValue() instanceof DecoratedLrAttribute)) {
                e.setValue(selfAttribute(e.getValue()));
            }
        }
    }

    private static void swapRelationshipReadersToSelf(ResourceEntity<?> entity) {
        entity.getChildren().values().forEach(child -> {
            LrRelationship incoming = child.getIncoming();
            if (incoming != null && !(incoming instanceof DecoratedLrRelationship)) {
                child.setIncoming(selfRelationship(incoming));
            }
        });
        entity.getAggregateChildren().values().forEach(child -> {
            LrRelationship incoming = child.getIncoming();
            if (incoming != null && !(incoming instanceof DecoratedLrRelationship)) {
                child.setIncoming(selfRelationship(incoming));
            }
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

    private static LrAttribute columnAttribute(LrAttribute attribute, int columnIndex) {
        PropertyReader reader = PropertyReader.forValueProducer((Object[] row) -> {
            if (row[0] instanceof DataObject) { // self has been included
                return row[columnIndex + 1];
            } else {
                return row[columnIndex];
            }
        });
        return decoratedAttribute(attribute, reader);
    }

    private static LrAttribute selfAttribute(LrAttribute attribute) {
        PropertyReader delegate = (attribute.getPropertyReader() == null) ?
                DataObjectPropertyReader.reader() : attribute.getPropertyReader();
        PropertyReader reader = (root, name) -> {
            Object[] row = (Object[]) root;
            return delegate.value(row[0], name);
        };
        return decoratedAttribute(attribute, reader);
    }

    private static LrRelationship selfRelationship(LrRelationship relationship) {
        PropertyReader delegate = (relationship.getPropertyReader() == null) ?
                DataObjectPropertyReader.reader() : relationship.getPropertyReader();
        PropertyReader reader = (root, name) -> {
            Object[] row = (Object[]) root;
            name = name.replace("@aggregated:", "");
            return delegate.value(row[0], name);
        };
        return decoratedRelationship(relationship, reader);
    }

    private static LrAttribute COUNT_ATTRIBUTE = new LrAttribute() {
        @Override
        public String getName() {
            return "count()";
        }

        @Override
        public Class<?> getType() {
            return Long.class;
        }

        @Override
        public ASTPath getPathExp() {
            return null;
        }

        @Override
        public PropertyReader getPropertyReader() {
            return null;
        }
    };

    private static LrAttribute decoratedAttribute(LrAttribute delegate, PropertyReader reader) {
        return new DecoratedLrAttribute(delegate, reader);
    }

    private static class DecoratedLrAttribute implements LrAttribute {

        private LrAttribute delegate;
        private PropertyReader reader;

        public DecoratedLrAttribute(LrAttribute delegate, PropertyReader reader) {
            this.delegate = delegate;
            this.reader = reader;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Class<?> getType() {
            return delegate.getType();
        }

        @Override
        public ASTPath getPathExp() {
            return delegate.getPathExp();
        }

        @Override
        public PropertyReader getPropertyReader() {
            return reader;
        }
    }

    private static LrRelationship decoratedRelationship(LrRelationship delegate, PropertyReader reader) {
        return new DecoratedLrRelationship(delegate, reader);
    }

    private static class DecoratedLrRelationship implements LrRelationship {

        private LrRelationship delegate;
        private PropertyReader reader;

        public DecoratedLrRelationship(LrRelationship delegate, PropertyReader reader) {
            this.delegate = delegate;
            this.reader = reader;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public LrEntity<?> getTargetEntity() {
            return delegate.getTargetEntity();
        }

        @Override
        public boolean isToMany() {
            return delegate.isToMany();
        }

        @Override
        public PropertyReader getPropertyReader() {
            return reader;
        }
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
