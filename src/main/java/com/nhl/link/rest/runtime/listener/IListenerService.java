package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * @since 1.19
 */
public interface IListenerService {

	Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> getListenerInvocationFactories(Class<?> listenerType,
			EventGroup eventGroup);
}
