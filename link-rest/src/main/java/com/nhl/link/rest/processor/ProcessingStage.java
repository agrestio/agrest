package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;

/**
 * A single stage in a chained request processor.
 *
 * @since 1.16
 * @deprecated since 2.7 in favor of {@link Processor}.
 */
public interface ProcessingStage<C extends ProcessingContext<T>, T> {

    /**
     * Executes this stage, returning the next stage to be executed or null if
     * the chain should be terminated.
     */
    ProcessingStage<C, ? super T> execute(C context);

    /**
     * Returns an Annotation class that can be used by listeners to annotate their methods that should be invoked after
     * the stage successfully finishes execution. If the stage doesn't support listeners, this method should return null.
     * Default implementation returns null.
     *
     * @return this implementation returns null.
     */
    default Class<? extends Annotation> afterStageListener() {
        return null;
    }
}
