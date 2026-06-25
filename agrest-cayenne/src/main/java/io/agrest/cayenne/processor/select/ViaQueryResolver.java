package io.agrest.cayenne.processor.select;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.ObjectSelect;

import java.util.List;

/**
 * A root resolver that builds and executes a Cayenne DB query based on the request parameters provided by the client.
 *
 * @since 3.4
 */
public class ViaQueryResolver<T extends Persistent> extends BaseRootDataResolver<T> {

    protected final ICayenneQueryAssembler queryAssembler;
    protected final ICayennePersister persister;

    public ViaQueryResolver(ICayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    @Override
    protected void doAssembleQuery(SelectContext<T> context) {
        CayenneProcessor.getRootEntity(context.getEntity()).setSelect(queryAssembler.createRootQuery(context));
    }

    @Override
    protected List<T> doFetchData(SelectContext<T> context) {
        ObjectSelect<T> select = CayenneProcessor.getRootEntity(context.getEntity()).getSelect();
        List<T> result = persister.sharedContext().select(select);
        return result;
    }
}
