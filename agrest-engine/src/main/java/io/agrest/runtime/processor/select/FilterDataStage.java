package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IResultFilter;
import org.apache.cayenne.di.Inject;

/**
 * @since 4.8
 */
public class FilterDataStage implements Processor<SelectContext<?>> {

    private final IResultFilter resultFilter;

    public FilterDataStage(@Inject IResultFilter resultFilter) {
        this.resultFilter = resultFilter;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        resultFilter.filterTree(context.getEntity());
    }
}
