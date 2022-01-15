package io.agrest.processor;

import io.agrest.runtime.AgExceptionMappers;

/**
 * @since 5.0
 */
public class ExceptionMappingProcessorDecorator<C extends ProcessingContext<?>> implements Processor<C> {

    private final Processor<C> delegate;
    private final AgExceptionMappers exceptionMappers;

    public ExceptionMappingProcessorDecorator(Processor<C> delegate, AgExceptionMappers exceptionMappers) {
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
