package io.agrest.processor;

import io.agrest.AgException;
import io.agrest.runtime.ExceptionMappers;

/**
 * @since 5.0
 */
public class ExceptionMappingProcessorDecorator<C extends ProcessingContext<?>> implements Processor<C> {

    private final Processor<C> delegate;
    private final ExceptionMappers exceptionMappers;

    public ExceptionMappingProcessorDecorator(Processor<C> delegate, ExceptionMappers exceptionMappers) {
        this.delegate = delegate;
        this.exceptionMappers = exceptionMappers;
    }

    @Override
    public ProcessorOutcome execute(C context) {
        try {
            return delegate.execute(context);
        } catch (Throwable th) {
            throw exceptionMappers.toAgException(th);
        }
    }

    @Override
    public Processor<C> andThen(Processor<C> after) {
        return new ExceptionMappingProcessorDecorator<>(delegate.andThen(after), exceptionMappers);
    }
}
