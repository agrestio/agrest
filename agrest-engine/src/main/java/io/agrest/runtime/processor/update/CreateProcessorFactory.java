package io.agrest.runtime.processor.update;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;

import java.util.EnumMap;

/**
 * @since 5.0
 */
public class CreateProcessorFactory extends BaseUpdateProcessorFactory {

    public CreateProcessorFactory(
            EnumMap<UpdateStage, Processor<UpdateContext<?>>> defaultStages,
            AgExceptionMappers exceptionMappers) {
        super(defaultStages, exceptionMappers);
    }
}
