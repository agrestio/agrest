package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.Select;

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

    <T> Select<T> buildQuery(SelectContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        QueryBuilder<T> query = new QueryBuilder<>(context);

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

    <T> Select<T> basicSelect(SelectContext<T> context) {
        return new QueryBuilder<>(context).buildQuery();
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
}
