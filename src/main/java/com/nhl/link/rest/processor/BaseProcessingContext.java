package com.nhl.link.rest.processor;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.16
 */
public abstract class BaseProcessingContext<T> implements ProcessingContext<T> {

	private Class<T> type;
	private Map<String, Object> attributes;

	public BaseProcessingContext(Class<T> type) {
		this.type = type;
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

		// presumably BaseProcessingContext is single-threaded, so lazy init and using 
		// like this is ok
		if (attributes == null) {
			attributes = new HashMap<>();
		}

		attributes.put(name, value);
	}
}
