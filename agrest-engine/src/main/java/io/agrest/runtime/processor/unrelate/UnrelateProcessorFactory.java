package io.agrest.runtime.processor.unrelate;

import io.agrest.UnrelateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorFactory;
import io.agrest.runtime.ExceptionMappers;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class UnrelateProcessorFactory extends ProcessorFactory<UnrelateStage, UnrelateContext<?>> {

    public UnrelateProcessorFactory(
            EnumMap<UnrelateStage, Processor<UnrelateContext<?>>> defaultStages,
            ExceptionMappers exceptionMappers) {
        super(defaultStages, exceptionMappers);
    }
}