package com.nhl.link.rest.runtime.parser.pointer;

public interface PointerContext {

	/**
	 * Finds and returns object of the specified type by its ID.
	 */
	<T> T resolveObject(Class<T> type, Object id);

	/**
	 * @param baseObject
	 *            Base object for resolving the specified attribute. Should be
	 *            from this PointerContext's Cayenne context
	 */
	Object resolveProperty(Object baseObject, String propertyName);
}
