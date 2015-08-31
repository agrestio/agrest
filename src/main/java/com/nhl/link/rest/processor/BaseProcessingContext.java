package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nhl.link.rest.runtime.listener.ListenerInvocation;

/**
 * @since 1.16
 */
public abstract class BaseProcessingContext<T> implements ProcessingContext<T> {

	private Class<T> type;
	private Map<String, Object> attributes;
	private Map<Class<? extends Annotation>, List<ListenerInvocation>> listeners;

	public BaseProcessingContext(Class<T> type) {
		this.type = type;
	}

	/**
	 * @since 1.19
	 */
	@Override
	public Map<Class<? extends Annotation>, List<ListenerInvocation>> getListeners() {
		return listeners == null ? Collections.<Class<? extends Annotation>, List<ListenerInvocation>> emptyMap()
				: listeners;
	}

	/**
	 * @since 1.19
	 */
	@Override
	public void setListeners(Map<Class<? extends Annotation>, List<ListenerInvocation>> listeners) {
		this.listeners = listeners;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes != null ? attributes.get(name) : null;
	}

	@Override
	public void setAttribute(String name, Object value) {

		// presumably BaseProcessingContext is single-threaded, so lazy init and
		// using
		// like this is ok
		if (attributes == null) {
			attributes = new HashMap<>();
		}

		attributes.put(name, value);
	}
}
