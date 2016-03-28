package com.nhl.link.rest.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.runtime.constraints.RequestConstraintsVisitor;

/**
 * Defines read or write constraints on a given entity. Constraints are
 * predefined on the server side and are applied to each request, ensuring a
 * client can't read or write more data than she is allowed to.
 * <p>
 * {@link ConstraintsBuilder} is transformed into
 * {@link RequestConstraintsVisitor} that is later consumed by LinkRest.
 * 
 * @since 1.3
 */
public class ConstraintsBuilder<T> implements Constraint {

	private Class<T> type;
	private Collection<Constraint> ops;

	/**
	 * @param type
	 *            a root type for constraints.
	 * @param <T>
	 *            LinkRest entity type.
	 * @return this builder instance.
	 * @since 1.5
	 */
	public static <T> ConstraintsBuilder<T> excludeAll(Class<T> type) {
		return new ConstraintsBuilder<>(type);
	}

	/**
	 * @param type
	 *            a root type for constraints.
	 * @param <T>
	 *            LinkRest entity type.
	 * @return this builder instance.
	 * @since 1.5
	 */
	public static <T> ConstraintsBuilder<T> idOnly(Class<T> type) {
		return excludeAll(type).includeId();
	}

	/**
	 * @param type
	 *            a root type for constraints.
	 * @param <T>
	 *            LinkRest entity type.
	 * @return this builder instance.
	 * @since 1.5
	 */
	public static <T> ConstraintsBuilder<T> idAndAttributes(Class<T> type) {
		return excludeAll(type).includeId().allAttributes();
	}

	protected ConstraintsBuilder(Class<T> type) {
		this.ops = new ArrayList<>();
		this.type = type;
	}

	/**
	 * @return a root type for constraints in this builder.
	 * @since 1.5
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Applies visitor to all collected constraints.
	 */
	@Override
	public void accept(ConstraintVisitor visitor) {
		for (Constraint c : ops) {
			c.accept(visitor);
		}
	}

	/**
	 * @param attributeOrRelationship
	 *            a name of the property to exclude.
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeProperty(final String attributeOrRelationship) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitExcludePropertiesConstraint(attributeOrRelationship);
			}
		});

		return this;
	}

	/**
	 * Excludes an attribute or relationship.
	 * 
	 * @param attributeOrRelationship
	 *            a name of the property to exclude.
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeProperty(final Property<?> attributeOrRelationship) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitExcludePropertiesConstraint(attributeOrRelationship.getName());
			}
		});
		return this;
	}

	/**
	 * @param attributesOrRelationships
	 *            an array of properties to exclude.
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeProperties(final String... attributesOrRelationships) {

		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitExcludePropertiesConstraint(attributesOrRelationships);
			}
		});

		return this;
	}

	/**
	 * @param attributesOrRelationships
	 *            an array of properties to exclude.
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeProperties(final Property<?>... attributesOrRelationships) {

		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {

				String[] names = new String[attributesOrRelationships.length];
				for (int i = 0; i < attributesOrRelationships.length; i++) {
					names[i] = attributesOrRelationships[i].getName();
				}

				visitor.visitExcludePropertiesConstraint(names);
			}
		});

		return this;
	}

	/**
	 * Excludes all previously included attributes.
	 * 
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeAllAttributes() {

		ops.add(new Constraint() {
			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitExcludeAllAttributesConstraint();
			}
		});

		return this;
	}

	/**
	 * Excludes all previously included child configs.
	 * 
	 * @return this builder instance.
	 * @since 1.15
	 */
	public ConstraintsBuilder<T> excludeAllChildren() {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitExcludeAllChildrenConstraint();
			}
		});

		return this;
	}

	public ConstraintsBuilder<T> attribute(final String attribute) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeAttributesConstraint(attribute);
			}
		});

		return this;
	}

	public ConstraintsBuilder<T> attribute(final Property<?> attribute) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeAttributesConstraint(attribute.getName());
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> allAttributes() {
		ops.add(new Constraint() {
			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeAllAttributesConstraint();
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> attributes(final Property<?>... attributes) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {

				String[] names = new String[attributes.length];
				for (int i = 0; i < attributes.length; i++) {
					names[i] = attributes[i].getName();
				}

				visitor.visitIncludeAttributesConstraint(names);
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> attributes(final String... attributes) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeAttributesConstraint(attributes);
			}
		});

		return this;
	}

	public ConstraintsBuilder<T> includeId(final boolean include) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeIdConstraint(include);
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> includeId() {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeIdConstraint(true);
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> excludeId() {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitIncludeIdConstraint(false);
			}
		});

		return this;
	}

	public ConstraintsBuilder<T> and(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitAndQualifierConstraint(qualifier);
			}
		});
		return this;
	}

	public ConstraintsBuilder<T> or(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				visitor.visitOrQualifierConstraint(qualifier);
			}
		});

		return this;
	}

	public <S> ConstraintsBuilder<T> path(Property<S> path, ConstraintsBuilder<S> subentityBuilder) {
		return path(path.getName(), subentityBuilder);
	}

	public <S> ConstraintsBuilder<T> toManyPath(Property<List<S>> path, ConstraintsBuilder<S> subentityBuilder) {
		return path(path.getName(), subentityBuilder);
	}

	public ConstraintsBuilder<T> path(final String path, final ConstraintsBuilder<?> subentityBuilder) {

		ops.add(new Constraint() {

			@Override
			public void accept(ConstraintVisitor visitor) {
				subentityBuilder.accept(visitor.subtreeVisitor(path));
			}

		});

		return this;
	}
}
