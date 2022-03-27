package io.agrest.runtime.processor.select.stage;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IResultFilter;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

import java.util.List;

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

        // TODO: this check is out of place here... Needs its own SelectStage?
        checkObjectNotFound(context, context.getEntity().getResult());
    }

    protected void checkObjectNotFound(SelectContext<?> context, List<?> result) {
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
