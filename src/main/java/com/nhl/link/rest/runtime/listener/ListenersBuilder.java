package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nhl.link.rest.processor.ProcessingContext;

/**
 * @since 1.19
 */
public class ListenersBuilder {

	private ProcessingContext<?> context;
	private EventGroup eventGroup;
	private IListenerService listenerService;
	private Map<Class<? extends Annotation>, List<ListenerInvocation>> listeners;

	public ListenersBuilder(IListenerService listenerService, ProcessingContext<?> context, EventGroup eventGroup) {
		this.context = context;
		this.eventGroup = eventGroup;
		this.listenerService = listenerService;
		this.listeners = new HashMap<>();
	}

	public Map<Class<? extends Annotation>, List<ListenerInvocation>> getListeners() {
		return listeners;
	}

	public ListenersBuilder addListener(Object listener) {

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = listenerService
				.getFactories(listener.getClass(), context, eventGroup);

		if (factories.isEmpty()) {
			return this;
		}

		for (Entry<Class<? extends Annotation>, List<ListenerInvocationFactory>> e : factories.entrySet()) {

			if (e.getValue().isEmpty()) {
				continue;
			}

			List<ListenerInvocation> list = listeners.get(e.getKey());
			if (list == null) {
				list = new ArrayList<>();
				listeners.put(e.getKey(), list);
			}

			for (ListenerInvocationFactory factory : e.getValue()) {
				list.add(factory.toInvocation(listener));
			}
		}

		return this;
	}

}
