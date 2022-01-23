package io.agrest.runtime.processor.update.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IResultFilter;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class UpdateFilterResultStage implements Processor<UpdateContext<?>> {

    private final IResultFilter resultFilter;

    public UpdateFilterResultStage(@Inject IResultFilter resultFilter) {
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
