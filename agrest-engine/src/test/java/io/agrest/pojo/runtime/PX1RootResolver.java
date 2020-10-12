package io.agrest.pojo.runtime;

import io.agrest.pojo.model.PX1;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.List;

import static java.util.Arrays.asList;

public class PX1RootResolver extends BaseRootDataResolver<PX1> {

    private static List<PX1> all = asList(
            new PX1(1),
            new PX1(2),
            new PX1(3),
            new PX1(4),
            new PX1(5));

    @Override
    protected void doAssembleQuery(SelectContext<PX1> context) {
        // do nothing...
    }

    @Override
    protected List<PX1> doFetchData(SelectContext<PX1> context) {
        return all;
    }
}
