package com.nhl.link.rest.runtime.constraints;

import com.nhl.link.rest.EntityConstraint;

/**
 * @since 1.6
 */
class AllowAllEntityConstraint implements EntityConstraint {

	private static final EntityConstraint instance = new AllowAllEntityConstraint();

	public static EntityConstraint instance() {
		return instance;
	}

	private AllowAllEntityConstraint() {
		// not allowing others to create us
	}

	@Override
	public boolean allowsAllAttributes() {
		return true;
	}

	@Override
	public boolean allowsAttribute(String name) {
		return true;
	}

	@Override
	public boolean allowsId() {
		return true;
	}

	@Override
	public boolean allowsRelationship(String name) {
		return true;
	}

	@Override
	public String getEntityName() {
		throw new UnsupportedOperationException();
	}
}
