package io.agrest.resolver;

import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 3.4
 */
public interface RootDataResolver<T> {

    void assembleQuery(SelectContext<T> context);

    void fetchData(SelectContext<T> context);
}
