package com.nhl.link.rest.meta;

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of entity properties that are not derived from persistent
 * metadata. An {@link LrEntityOverlay} is provided to LinkRest by the app, and
 * are merged into a corresponding {@link LrPersistentEntity}.
 * 
 * @since 1.12
 */
public class LrEntityOverlay<T> {

	private Class<T> type;
	private Set<String> transientAtttributes;

	public LrEntityOverlay(Class<T> type) {
		this.type = type;
		this.transientAtttributes = new HashSet<>();
	}

	public Class<T> getType() {
		return type;
	}

	public Set<String> getTransientAttributes() {
		return transientAtttributes;
	}
}
