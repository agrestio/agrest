package com.nhl.link.rest.meta;

/**
 * @since 1.12
 */
public interface LrRelationship {

	String getName();

	/**
	 * @deprecated since 2.0 in favor of {@link #getTargetEntity()} that can
	 *             provide the type.
	 */
	@Deprecated
	default Class<?> getTargetEntityType() {
		return getTargetEntity().getType();
	}

	/**
	 * @since 2.0
	 */
	LrEntity<?> getTargetEntity();

	boolean isToMany();
}
