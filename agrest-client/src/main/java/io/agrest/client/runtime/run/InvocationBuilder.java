package io.agrest.client.runtime.run;

import io.agrest.client.protocol.AgcRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 2.0
 */
public class InvocationBuilder {

    public static InvocationBuilder target(WebTarget target) {
        return new InvocationBuilder(target);
    }

    private TargetBuilder targetBuilder;
    private Consumer<Invocation.Builder> config;

    private InvocationBuilder(WebTarget target) {
        targetBuilder = TargetBuilder.target(target);
    }

    public InvocationBuilder request(AgcRequest request) {
        targetBuilder.request(request);
        return this;
    }

    public InvocationBuilder config(Consumer<Invocation.Builder> config) {
        this.config = config;
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
        Invocation.Builder bldr = targetBuilder.build().request();
        if (config != null) {
            config.accept(bldr);
        }
        return bldr;
    }
}
