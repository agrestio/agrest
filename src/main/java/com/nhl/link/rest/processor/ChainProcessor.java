package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;
import java.util.List;

import com.nhl.link.rest.runtime.listener.ListenerInvocation;

/**
 * An executor of a processing chain that runs stages one-by-one, calling
 * listeners in between.
 * 
 * @since 1.19
 */
public class ChainProcessor {

	public static <C extends ProcessingContext<T>, T> void execute(ProcessingStage<C, ? super T> chainHead, C context) {

		ProcessingStage<C, ? super T> next = chainHead.execute(context);
		next = invokeListeners(chainHead, context, next);

		if (next != null) {
			execute(next, context);
		}
	}

	static <C extends ProcessingContext<T>, T> ProcessingStage<C, ? super T> invokeListeners(
			ProcessingStage<C, ? super T> stage, C context, ProcessingStage<C, ? super T> next) {

		Class<? extends Annotation> listenerType = stage.afterStageListener();

		if (listenerType != null) {
			List<ListenerInvocation> listeners = context.getListeners().get(listenerType);
			if (listeners != null && !listeners.isEmpty()) {

				for (ListenerInvocation i : listeners) {
					next = i.invoke(context, next);
				}

				return next;
			}
		}

		return next;
	}
}
