package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.QueryMetadata;

/**
 * Use this select chain listener to override default pagination strategy.
 * See {@link com.nhl.link.rest.runtime.cayenne.processor.CayenneQueryAssembleStage} for details.
 *
 * @since 1.23
 */
public class NoPaginationStrategy {

    @QueryAssembled
    public <T> ProcessingStage<SelectContext<T>, T> beforeFetch(SelectContext<T> context,
                                                                ProcessingStage<SelectContext<T>, T> next) {

        context.getSelect().setPageSize(QueryMetadata.PAGE_SIZE_DEFAULT);
        return next;
    }
}
