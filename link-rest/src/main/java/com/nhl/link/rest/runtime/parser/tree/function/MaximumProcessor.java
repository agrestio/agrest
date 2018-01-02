package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.AggregationType;

public class MaximumProcessor extends AggregateByAttributeProcessor {

    public MaximumProcessor() {
        super(AggregationType.MAXIMUM);
    }
}
