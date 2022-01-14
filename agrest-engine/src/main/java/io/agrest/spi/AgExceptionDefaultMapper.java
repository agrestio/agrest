package io.agrest.spi;

import io.agrest.AgException;

/**
 * A default mapper for an {@link io.agrest.AgException} itself. Just returns the exception unchanged. Can be overridden
 * to do transform a thrown AgException in some way before sending to the client.
 *
 * @sijce 5.0
 */
public class AgExceptionDefaultMapper implements AgExceptionMapper<AgException> {

    @Override
    public AgException toAgException(AgException e) {
        return e;
    }
}
