package io.agrest.meta;

public class DefaultLrOperation implements LrOperation {

    private LinkMethodType method;

    public DefaultLrOperation(LinkMethodType method) {
        this.method = method;
    }

    @Override
    public LinkMethodType getMethod() {
        return method;
    }
}
