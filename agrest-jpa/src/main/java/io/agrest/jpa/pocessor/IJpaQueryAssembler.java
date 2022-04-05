package io.agrest.jpa.pocessor;

import io.agrest.runtime.processor.select.SelectContext;
import jakarta.persistence.TypedQuery;

/**
 * @since 5.0
 */
public interface IJpaQueryAssembler {
    <T> TypedQuery<T> createRootQuery(SelectContext<T> context);
}
