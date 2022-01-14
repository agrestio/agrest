package io.agrest.runtime.processor.delete;

import io.agrest.DeleteStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorFactory;
import io.agrest.runtime.ExceptionMappers;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class DeleteProcessorFactory extends ProcessorFactory<DeleteStage, DeleteContext<?>> {

    public DeleteProcessorFactory(
            EnumMap<DeleteStage, Processor<DeleteContext<?>>> defaultStages,
            ExceptionMappers exceptionMappers) {
        super(defaultStages, exceptionMappers);
    }
}
