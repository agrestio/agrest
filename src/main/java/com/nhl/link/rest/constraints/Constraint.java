package com.nhl.link.rest.constraints;

/**
 * @since 1.12
 */
public interface Constraint {

	void accept(ConstraintsVisitor visitor);
}
