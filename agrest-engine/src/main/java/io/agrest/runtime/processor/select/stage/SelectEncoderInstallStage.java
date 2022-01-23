package io.agrest.runtime.processor.select.stage;

import io.agrest.encoder.Encoder;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 4.8
 */
public class SelectEncoderInstallStage implements Processor<SelectContext<?>> {

    private final IEncoderService encoderService;

    public SelectEncoderInstallStage(@Inject IEncoderService encoderService) {
        this.encoderService = encoderService;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {

        // make sure we create the encoder, even when the result is empty, as we need to encode the totals
        if (context.getEncoder() == null) {
            context.setEncoder(createEncoder(context));
        }

        return ProcessorOutcome.CONTINUE;
    }

    private <T> Encoder createEncoder(SelectContext<T> context) {
        return encoderService.dataEncoder(context.getEntity(), context);
    }
}
