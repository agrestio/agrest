package com.nhl.link.rest.client.runtime.run;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import com.nhl.link.rest.client.protocol.LrRequest;

/**
 * @since 2.0
 */
public class InvocationBuilder {

    public static InvocationBuilder target(WebTarget target) {
        return new InvocationBuilder(target);
    }

    private TargetBuilder targetBuilder;

    private InvocationBuilder(WebTarget target) {
        targetBuilder = TargetBuilder.target(target);
    }

    public InvocationBuilder constraint(LrRequest constraint) {
        targetBuilder.constraint(constraint);
        return this;
    }

    public LinkRestInvocation build() {

        WebTarget target = targetBuilder.build();
        Invocation invocation = target.request().buildGet();

        return invocation::invoke;
    }
}
