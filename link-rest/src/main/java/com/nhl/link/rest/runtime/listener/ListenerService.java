package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.nhl.link.rest.processor.ProcessingContext;

/**
 * @since 1.19
 */
public class ListenerService implements IListenerService {

	private ListenerInvocationFactoryCompiler compiler;
	private ConcurrentMap<String, Map<Class<? extends Annotation>, List<ListenerInvocationFactory>>>[] factories;

	@SuppressWarnings("unchecked")
	public ListenerService() {
		this.compiler = new ListenerInvocationFactoryCompiler();
		this.factories = new ConcurrentMap[EventGroup.values().length];
		for (int i = 0; i < factories.length; i++) {
			factories[i] = new ConcurrentHashMap<>();
		}
	}

	private String factoriesKey(Class<?> listenerType, ProcessingContext<?> context) {
		// TODO: we are ignoring context's own type here, leaving it up to the
		// user to provide the right context type; will need to include that in
		// the key as soon as compiler supports this bound checking
		return listenerType.getName() + "|" + context.getType().getName();
	}

	@Override
	public Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> getFactories(
			Class<?> listenerType, ProcessingContext<?> context, EventGroup eventGroup) {

		if (listenerType == null) {
			throw new NullPointerException("Null type");
		}

		if (eventGroup == null) {
			throw new NullPointerException("Null eventGroup");
		}

		ConcurrentMap<String, Map<Class<? extends Annotation>, List<ListenerInvocationFactory>>> factoriesForEventGroup = factories[eventGroup
				.ordinal()];

		String key = factoriesKey(listenerType, context);
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factoriesForListener = factoriesForEventGroup
				.get(key);

		if (factoriesForListener == null) {

			Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> newFactories = compiler
					.compileFactories(listenerType, context, eventGroup);
			Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> oldFactories = factoriesForEventGroup
					.putIfAbsent(key, newFactories);
			factoriesForListener = oldFactories != null ? oldFactories : newFactories;
		}

		return factoriesForListener;
	}

}
