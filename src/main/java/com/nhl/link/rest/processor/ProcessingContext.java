package com.nhl.link.rest.processor;

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
}
