package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.processor.ProcessingContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * @since 1.19
 * @deprecated since 2.7 as listeners got replaced by functional stages.
 */
public interface IListenerService {

    Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> getFactories(Class<?> listenerType,
                                                                                   ProcessingContext<?> context, EventGroup eventGroup);
}
