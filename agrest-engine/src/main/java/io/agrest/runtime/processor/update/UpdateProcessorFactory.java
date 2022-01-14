package io.agrest.runtime.processor.update;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorFactory;
import io.agrest.runtime.ExceptionMappers;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class UpdateProcessorFactory extends ProcessorFactory<UpdateStage, UpdateContext<?>> {

    public UpdateProcessorFactory(
            EnumMap<UpdateStage, Processor<UpdateContext<?>>> defaultStages,
            ExceptionMappers exceptionMappers) {
        super(defaultStages, exceptionMappers);
    }
}
