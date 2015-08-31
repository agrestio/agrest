package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;
import java.util.List;

import com.nhl.link.rest.runtime.listener.ListenerInvocation;

/**
 * A single stage in the chain of responsibility of select request processing.
 * 
 * @since 1.16
 */
public abstract class ProcessingStage<C extends ProcessingContext<T>, T> implements Processor<C, T> {

	private Processor<C, ? super T> next;

	public ProcessingStage(Processor<C, ? super T> next) {
		this.next = next;
	}

	/**
	 * Executes this stage and then delegates to the next Processor in the chain
	 * of responsibility. You should override
	 * {@link #doExecute(ProcessingContext)} to implement own execution, and
	 * override this method to change the default stage flow.
	 */
	@Override
	public void execute(C context) {

		// run our own processing, and then pass control down the chain...
		doExecute(context);

		Processor<C, ? super T> next = invokeListenersAndRoute(context);
		if (next != null) {
			next.execute(context);
		}
	}

	protected abstract void doExecute(C context);

	/**
	 * A method implemented by subclasses to returns a type of the listener
	 * annotation that is understood by a given stage. If a non-null value is
	 * returned, appropriate listeners will be called upon completion of that
	 * stage.
	 */
	protected Class<? extends Annotation> afterStageListener() {
		return null;
	}

	protected Processor<C, ? super T> invokeListenersAndRoute(C context) {

		Class<? extends Annotation> listenerType = afterStageListener();
		if (listenerType != null) {
			List<ListenerInvocation> listeners = context.getListeners().get(listenerType);
			if (listeners != null && !listeners.isEmpty()) {

				Processor<C, ? super T> next = this.next;

				for (ListenerInvocation i : listeners) {
					next = i.invoke(context, next);
				}

				return next;
			}
		}

		return this.next;
	}
}
