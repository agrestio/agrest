package com.nhl.link.rest.processor2;

import com.nhl.link.rest.processor.ProcessingContext;

import java.util.Objects;

/**
 * @since 2.7
 */
public interface Processor<C extends ProcessingContext<?>> {

    /**
     * Executes processor actions with a given mutable context.
     */
    ProcessorOutcome execute(C context);

    default Processor<C> andThen(Processor<C> after) {
        Objects.requireNonNull(after);
        return c -> execute(c) == ProcessorOutcome.STOP
                ? ProcessorOutcome.STOP
                : after.execute(c);
    }
}
