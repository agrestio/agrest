package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.AggregationType;

public class MinimumProcessor extends AggregateByAttributeProcessor {

    public MinimumProcessor() {
        super(AggregationType.MINIMUM);
    }
}
