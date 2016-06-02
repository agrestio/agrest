package com.nhl.link.rest.client.runtime.run;

import java.util.function.Supplier;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.nhl.link.rest.client.protocol.LrcRequest;

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

    public InvocationBuilder request(LrcRequest request) {
        targetBuilder.request(request);
        return this;
    }

    public Supplier<Response> build() {

        WebTarget target = targetBuilder.build();
        Invocation invocation = target.request().buildGet();

        return invocation::invoke;
    }
}
