package io.agrest.runtime.jackson;

import org.apache.cayenne.di.Provider;

/**
 * @since 5.0
 */
public class JacksonServiceProvider implements Provider<IJacksonService> {

    @Override
    public IJacksonService get() {
        return JacksonService.create();
    }
}
