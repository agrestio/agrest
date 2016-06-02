package com.nhl.link.rest.client;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

public class InvocationBuilder {

    public static InvocationBuilder target(WebTarget target) {
        return new InvocationBuilder(target);
    }

    private TargetBuilder targetBuilder;

    private InvocationBuilder(WebTarget target) {
        targetBuilder = TargetBuilder.target(target);
    }

    public InvocationBuilder constraint(Constraint constraint) {
        targetBuilder.constraint(constraint);
        return this;
    }

    public LinkRestInvocation build() {

        WebTarget target = targetBuilder.build();
        Invocation invocation = target.request().buildGet();

        return invocation::invoke;
    }
}
