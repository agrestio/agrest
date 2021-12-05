package io.agrest.runtime.processor.update;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IResultFilter;
import org.apache.cayenne.di.Inject;

/**
 * @since 4.8
 */
public class FilterResultStage implements Processor<UpdateContext<?>> {

    private final IResultFilter resultFilter;

    public FilterResultStage(@Inject IResultFilter resultFilter) {
        this.resultFilter = resultFilter;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        if (context.isIncludingDataInResponse()) {
            resultFilter.filterTree(context.getEntity());
        }
    }
}
