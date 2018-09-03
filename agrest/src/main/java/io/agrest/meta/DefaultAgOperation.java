package io.agrest.meta;

public class DefaultAgOperation implements AgOperation {

    private LinkMethodType method;

    public DefaultAgOperation(LinkMethodType method) {
        this.method = method;
    }

    @Override
    public LinkMethodType getMethod() {
        return method;
    }
}
