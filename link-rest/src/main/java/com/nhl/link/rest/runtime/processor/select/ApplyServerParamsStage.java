package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import org.apache.cayenne.di.Inject;

import java.util.List;

/**
 * @since 2.7
 */
public class ApplyServerParamsStage implements Processor<SelectContext<?>> {

    private IConstraintsHandler constraintsHandler;

    private List<EncoderFilter> filters;

    public ApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject List<EncoderFilter> filters) {

        this.constraintsHandler = constraintsHandler;
        this.filters = filters;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        constraintsHandler.constrainResponse(entity, context.getSizeConstraints(), context.getConstraint());

        if (context.getExtraProperties() != null) {
            entity.getExtraProperties().putAll(context.getExtraProperties());
        }

        for (EncoderFilter filter : filters) {
            if (filter.matches(entity)) {
                entity.setFiltered(true);
                break;
            }
        }
    }
}