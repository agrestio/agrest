package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import org.apache.cayenne.di.Inject;

import java.util.List;

/**
 * @since 2.7
 */
public class ApplyServerParamsStage implements Processor<SelectContext<?>> {

    private IConstraintsHandler constraintsHandler;
    private IEncoderService encoderService;
    private List<EncoderFilter> filters;

    public ApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject IEncoderService encoderService,
            @Inject List<EncoderFilter> filters) {

        this.constraintsHandler = constraintsHandler;
        this.encoderService = encoderService;
        this.filters = filters;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doBefore(context);
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    /**
     * @since 2.13
     */
    protected <T> void doBefore(SelectContext<T> context) {
        constraintsHandler.constrainRequest(context, context.getConstraint());
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

        // make sure we create the encoder, even if we end up with an empty
        // list, as we need to encode the totals

        if (context.getEncoder() == null) {
            context.setEncoder(encoderService.dataEncoder(entity));
        }
    }
}