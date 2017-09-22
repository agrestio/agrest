package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.AggregationType;

public class AverageProcessor extends AggregateByAttributeProcessor {

    public AverageProcessor() {
        super(AggregationType.AVERAGE);
    }
}
