package com.nhl.link.rest.client.runtime.run;

import com.nhl.link.rest.client.protocol.LrcRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

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

    public Supplier<Response> buildGet() {
        return toInvocation().buildGet()::invoke;
    }

    public Supplier<Response> buildPost(String data) {
        return toInvocation().buildPost(Entity.json(data))::invoke;
    }

    public Supplier<Response> buildPut(String data) {
        return toInvocation().buildPut(Entity.json(data))::invoke;
    }

    public Supplier<Response> buildDelete() {
        return toInvocation().buildDelete()::invoke;
    }

    private Invocation.Builder toInvocation() {
        return targetBuilder.build().request();
    }
}
