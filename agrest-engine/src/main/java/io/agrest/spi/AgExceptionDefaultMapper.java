package io.agrest.spi;

import io.agrest.AgException;

/**
 * A default mapper for an {@link io.agrest.AgException} itself. Just returns
 */
public class AgExceptionDefaultMapper implements AgExceptionMapper<AgException> {

    @Override
    public AgException toAgException(AgException e) {
        return e;
    }
}
