package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorFactory;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class SelectProcessorFactory extends ProcessorFactory<SelectStage, SelectContext<?>> {

    public SelectProcessorFactory(EnumMap<SelectStage, Processor<SelectContext<?>>> defaultStages) {
        super(defaultStages);
    }
}
