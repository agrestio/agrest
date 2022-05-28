package io.agrest.compiler;

import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;

/**
 * Produces {@link AgEntity} for a given Java type. Compilers are chained together, so if a given
 * compiler returns null, the next one is tried until a default annotation-based compiler is reached.
 *
 * @since 1.24
 */
public interface AgEntityCompiler {

    /**
     * Produces {@link AgEntity} for a given Java type. May return null to indicate that a given type
     * can not be handled by this compiler, and should be passed to the next compiler in the chain.
     *
     * @since 2.0
     */
    <T> AgEntity<T> compile(Class<T> type, AgSchema schema);
}
