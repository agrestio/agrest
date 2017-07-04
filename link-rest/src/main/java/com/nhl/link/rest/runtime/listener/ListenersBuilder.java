package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.processor.ProcessingContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @since 1.19
 * @deprecated since 2.7 as listeners got replaced by functional stages.
 */
public abstract class ListenersBuilder<E extends Enum<E>> {

	private ProcessingContext<?> context;
	private EventGroup eventGroup;
	private IListenerService listenerService;

	public ListenersBuilder(IListenerService listenerService, ProcessingContext<?> context, EventGroup eventGroup) {
		this.context = context;
		this.eventGroup = eventGroup;
		this.listenerService = listenerService;
	}

	protected abstract E mapStage(Class<? extends Annotation> annotation);

	protected abstract void appendInvocation(E stage, ListenerInvocation invocation);

    public ListenersBuilder addListener(Object listener) {

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories =
                listenerService
				.getFactories(listener.getClass(), context, eventGroup);

		if (factories.isEmpty()) {
			return this;
		}

		for (Entry<Class<? extends Annotation>, List<ListenerInvocationFactory>> e : factories.entrySet()) {

			if (e.getValue().isEmpty()) {
				continue;
			}

            E stage = mapStage(e.getKey());
			e.getValue().forEach(factory -> appendInvocation(stage, factory.toInvocation(listener)));
		}

		return this;
	}

}
