package io.agrest.runtime.processor.select.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IResultFilter;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 4.8
 */
public class SelectFilterResultStage implements Processor<SelectContext<?>> {

    private final IResultFilter resultFilter;

    public SelectFilterResultStage(@Inject IResultFilter resultFilter) {
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
