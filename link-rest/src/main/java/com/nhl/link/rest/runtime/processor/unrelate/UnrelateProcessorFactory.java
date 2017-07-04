package com.nhl.link.rest.runtime.processor.unrelate;

import com.nhl.link.rest.UnrelateStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorFactory;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class UnrelateProcessorFactory extends ProcessorFactory<UnrelateStage, UnrelateContext<?>> {

    public UnrelateProcessorFactory(EnumMap<UnrelateStage, Processor<UnrelateContext<?>>> defaultStages) {
        super(defaultStages);
    }
}