package com.nhl.link.rest.constraints;

import org.apache.cayenne.exp.Expression;

/**
 * @since 1.12
 */
public interface ConstraintsVisitor {

	void visitExcludeAllConstraint();

	void visitExcludeChildrenConstraint();

	void visitAttributesConstraint(String... attributes);

	void visitAllAttributesConstraint();

	void visitIncludeIdConstraint(boolean include);

	void visitAndQualifierConstraint(Expression qualifier);

	void visitOrQualifierConstraint(Expression qualifier);

	ConstraintsVisitor subtreeVisitor(String path);
}
