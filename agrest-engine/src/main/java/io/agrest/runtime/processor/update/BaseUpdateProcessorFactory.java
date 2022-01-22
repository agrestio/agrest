package io.agrest.runtime.processor.update;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorFactory;
import io.agrest.runtime.AgExceptionMappers;

import java.util.EnumMap;

/**
 * @since 5.0
 */
public abstract class BaseUpdateProcessorFactory extends ProcessorFactory<UpdateStage, UpdateContext<?>> {

    protected BaseUpdateProcessorFactory(
            EnumMap<UpdateStage, Processor<UpdateContext<?>>> defaultStages, AgExceptionMappers exceptionMappers) {
        super(defaultStages, exceptionMappers);
    }
}
