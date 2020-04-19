package io.agrest.runtime.processor.select;

import io.agrest.SelectStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorFactory;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class SelectProcessorFactory extends ProcessorFactory<SelectStage, SelectContext<?>> {

    public SelectProcessorFactory(EnumMap<SelectStage, Processor<SelectContext<?>>> defaultStages) {
        super(defaultStages);
    }
}
