package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;

import javax.ws.rs.core.Response;

public class CountProcessor implements FunctionProcessor {

    @Override
    public void apply(ResourceEntity<?> context) {
        context.includeCount();
    }

    @Override
    public void apply(ResourceEntity<?> context, LrAttribute attribute) {
        throw new LinkRestException(Response.Status.BAD_REQUEST, "Function is not applicable in this context");
    }
}
