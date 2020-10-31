package io.agrest.meta;

/**
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
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
