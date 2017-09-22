package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.AggregationType;

public class SumProcessor extends AggregateByAttributeProcessor {

    public SumProcessor() {
        super(AggregationType.SUM);
    }
}
