package com.nhl.link.rest;

import com.nhl.link.rest.constraints.ConstraintsBuilder;

/**
 * @deprecated since 1.12 use {@link ConstraintsBuilder}
 */
@Deprecated
public class TreeConstraints<T> extends ConstraintsBuilder<T> {

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> excludeAll(Class<T> type) {
		return new TreeConstraints<>(type);
	}

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> idOnly(Class<T> type) {
		return (TreeConstraints<T>) excludeAll(type).includeId();
	}

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> idAndAttributes(Class<T> type) {
		return (TreeConstraints<T>) excludeAll(type).includeId().allAttributes();
	}

	TreeConstraints(Class<T> type) {
		super(type);
	}
}
