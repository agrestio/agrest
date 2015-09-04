package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;

/**
 * A single stage in the chain of responsibility of select request processing.
 * 
 * @since 1.19
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

	/**
	 * Returns null in the base implementation. Stages that support listeners
	 * should override this.
	 */
	@Override
	public Class<? extends Annotation> afterStageListener() {
		return null;
	}
}
