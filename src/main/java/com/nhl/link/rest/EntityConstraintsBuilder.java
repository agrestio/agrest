package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjEntity;

/**
 * A builder for {@link EntityConstraints}.
 * 
 * @since 1.2
 */
public class EntityConstraintsBuilder {

	private Collection<Constraint> ops;

	public static EntityConstraintsBuilder constraints() {
		return new EntityConstraintsBuilder();
	}

	public static EntityConstraintsBuilder excludeAll() {
		return constraints().excludeAttributes().excludeChildren();
	}

	private EntityConstraintsBuilder() {
		this.ops = new ArrayList<>();
	}

	/**
	 * Creates a new {@link EntityConstraints} instance based on the builder
	 * configuration.
	 */
	public EntityConstraints build(ObjEntity entity) {
		return build(new EntityConstraints(entity));
	}

	EntityConstraints build(EntityConstraints constraints) {

		for (Constraint c : ops) {
			c.append(constraints);
		}

		return constraints;
	}

	public EntityConstraintsBuilder append(EntityConstraintsBuilder another) {
		ops.addAll(another.ops);
		return this;
	}

	/**
	 * Excludes all previously included attributes.
	 */
	public EntityConstraintsBuilder excludeAttributes() {

		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.excludeAttributes();
			}
		});

		return this;
	}

	/**
	 * Excludes all previously included child configs.
	 */
	public EntityConstraintsBuilder excludeChildren() {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.excludeChildren();
			}
		});

		return this;
	}

	public EntityConstraintsBuilder attribute(final String attribute) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.attribute(attribute);
			}
		});

		return this;
	}

	public EntityConstraintsBuilder attribute(final Property<?> attribute) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.attribute(attribute);
			}
		});
		return this;
	}

	public EntityConstraintsBuilder attributes(final Property<?>... attributes) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.attributes(attributes);
			}
		});
		return this;
	}

	public EntityConstraintsBuilder attributes(final String... attributes) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.attributes(attributes);
			}
		});

		return this;
	}

	public EntityConstraintsBuilder includeId(final boolean include) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.includeId(include);
			}
		});
		return this;
	}

	public EntityConstraintsBuilder includeId() {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.includeId();
			}
		});
		return this;
	}

	public EntityConstraintsBuilder excludeId() {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.excludeId();
			}
		});

		return this;
	}

	public EntityConstraintsBuilder and(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.and(qualifier);
			}
		});
		return this;
	}

	public EntityConstraintsBuilder or(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				constraints.or(qualifier);
			}
		});

		return this;
	}

	public EntityConstraintsBuilder path(Property<?> path, EntityConstraintsBuilder subentityBuilder) {
		return path(path.getName(), subentityBuilder);
	}

	public EntityConstraintsBuilder path(final String path, final EntityConstraintsBuilder subentityBuilder) {

		ops.add(new Constraint() {

			@Override
			public void append(EntityConstraints constraints) {
				EntityConstraints subConstraints = constraints.ensurePath(path);
				subentityBuilder.build(subConstraints);
			}
		});

		return this;
	}

	// a functional interface to store builder operations
	interface Constraint {
		void append(EntityConstraints constraints);
	}
}
