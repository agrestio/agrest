package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.backend.util.converter.ExpressionConverter;
import io.agrest.backend.util.converter.OrderingConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneAssembleQueryStage implements Processor<SelectContext<?>> {

    private EntityResolver entityResolver;
    private ExpressionConverter<Expression> expressionConverter;
    private OrderingConverter<Ordering> orderingConverter;

    public CayenneAssembleQueryStage(@Inject ICayennePersister persister,
                                     @Inject ExpressionConverter expressionConverter,
                                     @Inject OrderingConverter orderingConverter) {
        this.entityResolver = persister.entityResolver();
        this.expressionConverter = expressionConverter;
        this.orderingConverter = orderingConverter;
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

        SelectQuery<T> query = basicSelect(context);

        if (!entity.isFiltered()) {
            int limit = context.getEntity().getFetchLimit();
            if (limit > 0) {
                query.setPageSize(limit);
            }
        }

        if (context.getParent() != null) {
            Expression qualifier = context.getParent().qualifier(entityResolver);
            query.andQualifier(qualifier);
        }

        if (entity.getQualifier() != null) {
            query.andQualifier(expressionConverter.apply(entity.getQualifier()));
        }

        query.addOrderings(orderingConverter.apply(entity.getOrderings()));

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
            query.setPrefetchTree(root);
        }

        return query;
    }

    <T> SelectQuery<T> basicSelect(SelectContext<T> context) {

        // selecting by ID overrides any explicit SelectQuery...
        if (context.isById()) {

            Class<T> root = context.getType();
            SelectQuery<T> query = new SelectQuery<>(root);
            query.andQualifier(buildIdQualifer(context.getEntity().getAgEntity(), context.getId()));
            return query;
        }

        return context.getSelect() != null ? context.getSelect() : new SelectQuery<>(context.getType());
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

    private void appendPrefetches(PrefetchTreeNode root, ResourceEntity<?> entity, int prefetchSemantics) {
        for (Map.Entry<String, ResourceEntity<?>> e : entity.getChildren().entrySet()) {

            // skip prefetches of non-persistent entities
            if (e.getValue().getAgEntity() instanceof AgPersistentEntity) {

                PrefetchTreeNode child = root.addPath(e.getKey());

                // always full prefetch related entities... we can't use phantom
                // as this will hit object cache and hence won't be cache
                // controlled via query cache anymore...
                child.setPhantom(false);
                child.setSemantics(prefetchSemantics);
                appendPrefetches(child, e.getValue(), prefetchSemantics);
            }

            // converts expressions
        }

        if (entity.getMapBy() != null) {
            appendPrefetches(root, entity.getMapBy(), prefetchSemantics);
        }
    }
}
