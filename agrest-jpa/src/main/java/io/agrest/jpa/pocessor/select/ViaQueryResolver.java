package io.agrest.jpa.pocessor.select;

import java.util.List;

import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import jakarta.persistence.TypedQuery;

/**
 * @since 5.0
 */
public class ViaQueryResolver <T> extends BaseRootDataResolver<T> {

    protected final IJpaQueryAssembler queryAssembler;

    public ViaQueryResolver(IJpaQueryAssembler queryAssembler) {
        this.queryAssembler = queryAssembler;
    }

    @Override
    protected void doAssembleQuery(SelectContext<T> context) {
        JpaProcessor.getRootEntity(context.getEntity()).setSelect(queryAssembler.createRootQuery(context));
    }

    @Override
    protected List<T> doFetchData(SelectContext<T> context) {
        TypedQuery<T> select = JpaProcessor.getRootEntity(context.getEntity()).getSelect();
        return select.getResultList();
    }
}
