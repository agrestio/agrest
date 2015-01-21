package com.nhl.link.rest.meta;

import java.util.HashSet;
import java.util.Set;

/**
 * A model of entity properties that can not be derived from persistent
 * metadata.
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
