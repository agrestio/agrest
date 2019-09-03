package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.encoder.EncoderFilter;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
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
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        constraintsHandler.constrainResponse(entity, context.getSizeConstraints(), context.getConstraint());

        initEncoderFilters(context);

        // make sure we create the encoder, even if we end up with an empty
        // list, as we need to encode the totals

        if (context.getEncoder() == null) {
            context.setEncoder(encoderService.dataEncoder(entity));
        }
    }

    protected void initEncoderFilters(SelectContext<?> context) {
        List<EncoderFilter> filters = mergeFilters(context.getEncoderFilters());
        if (!filters.isEmpty()) {
            initEncoderFilters(context.getEntity(), filters);
        }
    }

    protected void initEncoderFilters(ResourceEntity<?> entity, List<EncoderFilter> filters) {

        for (EncoderFilter filter : filters) {
            if (filter.matches(entity)) {
                entity.getEncoderFilters().add(filter);
            }
        }

        for (ResourceEntity<?> child : entity.getChildren().values()) {
            initEncoderFilters(child, filters);
        }
    }

    protected List<EncoderFilter> mergeFilters(List<EncoderFilter> requestFilters) {

        if (requestFilters == null || requestFilters.isEmpty()) {
            return this.filters;
        }

        if (this.filters.isEmpty()) {
            return requestFilters;
        }

        List<EncoderFilter> combined = new ArrayList<>(requestFilters.size() + filters.size());

        // global filters go first, per-request filters applied after them
        combined.addAll(filters);
        combined.addAll(requestFilters);

        return combined;
    }
}