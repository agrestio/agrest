package com.nhl.link.rest.processor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import com.nhl.link.rest.runtime.listener.ListenerInvocation;

/**
 * An abstraction of a "context" object in a processor chain.
 *
 * @param <T>
 *            type of entity being processed.
 */
public interface ProcessingContext<T> {

	Class<T> getType();

	/**
	 * Returns a previously stored context attribute or null if none was set for
	 * a given key.
	 */
	Object getAttribute(String name);

	/**
	 * Allows to store an arbitrary attribute in the context during processing.
	 */
	void setAttribute(String name, Object value);

	/**
	 * @since 1.19
	 */
	Map<Class<? extends Annotation>, List<ListenerInvocation>> getListeners();
}
