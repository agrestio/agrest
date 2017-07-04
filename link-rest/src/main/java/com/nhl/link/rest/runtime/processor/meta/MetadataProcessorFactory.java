package com.nhl.link.rest.runtime.processor.meta;

import com.nhl.link.rest.MetadataStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorFactory;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class MetadataProcessorFactory extends ProcessorFactory<MetadataStage, MetadataContext<?>> {

    public MetadataProcessorFactory(EnumMap<MetadataStage, Processor<MetadataContext<?>>> defaultStages) {
        super(defaultStages);
    }
}
