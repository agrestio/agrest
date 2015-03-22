package com.nhl.link.rest.constraints;

import org.apache.cayenne.exp.Expression;

/**
 * @since 1.12
 */
public interface ConstraintVisitor {

	/**
	 * @since 1.15
	 */
	void visitExcludePropertiesConstraint(String... attributesOrRelationships);

	void visitExcludeAllAttributesConstraint();

	void visitExcludeAllChildrenConstraint();

	void visitIncludeAttributesConstraint(String... attributes);

	void visitIncludeAllAttributesConstraint();

	void visitIncludeIdConstraint(boolean include);

	void visitAndQualifierConstraint(Expression qualifier);

	void visitOrQualifierConstraint(Expression qualifier);

	ConstraintVisitor subtreeVisitor(String path);
}
