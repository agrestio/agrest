package com.nhl.link.rest.processor;

/**
 * A single stage in the chain of responsibility of select request processing.
 *
 * @since 1.19
 * @deprecated since 2.7, as the processing pipeline has been refactored to {@link Processor}.
 */
public abstract class BaseLinearProcessingStage<C extends ProcessingContext<T>, T> implements ProcessingStage<C, T> {

    private ProcessingStage<C, ? super T> next;

    public BaseLinearProcessingStage(ProcessingStage<C, ? super T> next) {
        this.next = next;
    }

    @Override
    public ProcessingStage<C, ? super T> execute(C context) {
        doExecute(context);
        return next;
    }

    protected abstract void doExecute(C context);
}
