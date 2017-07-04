package com.nhl.link.rest.runtime.processor.delete;

import com.nhl.link.rest.DeleteStage;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorFactory;

import java.util.EnumMap;

public class DeleteProcessorFactory extends ProcessorFactory<DeleteStage, DeleteContext<?>> {

    public DeleteProcessorFactory(EnumMap<DeleteStage, Processor<DeleteContext<?>>> defaultStages) {
        super(defaultStages);
    }
}
