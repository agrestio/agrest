package io.agrest.jpa.pocessor.select;

import java.util.List;

import io.agrest.AgException;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgEntity;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public class ViaQueryResolver <T> extends BaseRootDataResolver<T> {

    protected final IJpaQueryAssembler queryAssembler;
    protected final IAgJpaPersister persister;

    public ViaQueryResolver(IJpaQueryAssembler queryAssembler, IAgJpaPersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doAssembleQuery(SelectContext<T> context) {
        JpaProcessor.getRootEntity(context.getEntity()).setSelect(queryAssembler.createRootQuery(context));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<T> doFetchData(SelectContext<T> context) {
        JpaQueryBuilder select = JpaProcessor.getRootEntity(context.getEntity()).getSelect();
        List result = select.build(persister.entityManager()).getResultList();
        checkObjectNotFound(context, result);
        return result;
    }

    protected void checkObjectNotFound(SelectContext<T> context, List<?> result) {
        if (context.isAtMostOneObject() && result.size() != 1) {

            AgEntity<?> entity = context.getEntity().getAgEntity();

            if (result.isEmpty()) {
                throw AgException.notFound("No object for ID '%s' and entity '%s'", context.getId(), entity.getName());
            } else {
                throw AgException.internalServerError("Found more than one object for ID '%s' and entity '%s'",
                        context.getId(),
                        entity.getName());
            }
        }
    }
}
