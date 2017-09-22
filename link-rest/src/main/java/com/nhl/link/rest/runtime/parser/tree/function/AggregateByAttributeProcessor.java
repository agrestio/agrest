package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.AggregationType;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;

import javax.ws.rs.core.Response.Status;

public class AggregateByAttributeProcessor implements FunctionProcessor {

    private final AggregationType aggregationType;

    public AggregateByAttributeProcessor(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    @Override
    public void apply(ResourceEntity<?> context) {
        throw new LinkRestException(Status.BAD_REQUEST, "Function is not applicable in this context");
    }

    @Override
    public void apply(ResourceEntity<?> context, LrAttribute attribute) {
        context.getAggregatedAttributes(aggregationType).add(attribute);
    }
}
