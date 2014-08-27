package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;

/**
 * Defines read or write constraints on a given entity. Constraints are
 * predefined on the server side and are applied to each request, ensuring a
 * client can't read or write more data than she is allowed to.
 * <p>
 * {@link TreeConstraints} is transformed into {@link ImmutableTreeConstraints}
 * that is later consumed by LinkRest.
 * 
 * @since 1.3
 */
public class TreeConstraints<T> {

	private Collection<Constraint> ops;

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> excludeAll(Class<T> type) {
		return new TreeConstraints<>();
	}

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> idOnly(Class<T> type) {
		return excludeAll(type).includeId();
	}

	/**
	 * @since 1.5
	 */
	public static <T> TreeConstraints<T> idAndAttributes(Class<T> type) {
		return excludeAll(type).includeId();
	}

	private TreeConstraints() {
		this.ops = new ArrayList<>();
	}

	/**
	 * Creates a new {@link ImmutableTreeConstraints} instance based on the
	 * builder configuration.
	 */
	public ImmutableTreeConstraints build(DataResponse<T> response) {
		return build(new ImmutableTreeConstraints(response.getEntity().getCayenneEntity()));
	}

	ImmutableTreeConstraints build(ImmutableTreeConstraints constraints) {

		for (Constraint c : ops) {
			c.append(constraints);
		}

		return constraints;
	}

	/**
	 * Excludes all previously included attributes.
	 */
	public TreeConstraints<T> excludeAttributes() {

		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.excludeAttributes();
			}
		});

		return this;
	}

	/**
	 * Excludes all previously included child configs.
	 */
	public TreeConstraints<T> excludeChildren() {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.excludeChildren();
			}
		});

		return this;
	}

	public TreeConstraints<T> attribute(final String attribute) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.attribute(attribute);
			}
		});

		return this;
	}

	public TreeConstraints<T> attribute(final Property<?> attribute) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.attribute(attribute);
			}
		});
		return this;
	}

	public TreeConstraints<T> allAttributes() {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.allAttributes();
			}
		});
		return this;
	}

	public TreeConstraints<T> attributes(final Property<?>... attributes) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.attributes(attributes);
			}
		});
		return this;
	}

	public TreeConstraints<T> attributes(final String... attributes) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.attributes(attributes);
			}
		});

		return this;
	}

	public TreeConstraints<T> includeId(final boolean include) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.includeId(include);
			}
		});
		return this;
	}

	public TreeConstraints<T> includeId() {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.includeId();
			}
		});
		return this;
	}

	public TreeConstraints<T> excludeId() {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.excludeId();
			}
		});

		return this;
	}

	public TreeConstraints<T> and(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.and(qualifier);
			}
		});
		return this;
	}

	public TreeConstraints<T> or(final Expression qualifier) {
		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				constraints.or(qualifier);
			}
		});

		return this;
	}

	public <S> TreeConstraints<T> path(Property<S> path, TreeConstraints<S> subentityBuilder) {
		return path(path.getName(), subentityBuilder);
	}

	public TreeConstraints<T> path(final String path, final TreeConstraints<?> subentityBuilder) {

		ops.add(new Constraint() {

			@Override
			public void append(ImmutableTreeConstraints constraints) {
				ImmutableTreeConstraints subConstraints = constraints.ensurePath(path);
				subentityBuilder.build(subConstraints);
			}
		});

		return this;
	}

	// a functional interface to store builder operations
	interface Constraint {
		void append(ImmutableTreeConstraints constraints);
	}
}
