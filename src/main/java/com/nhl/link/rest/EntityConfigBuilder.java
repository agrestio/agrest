package com.nhl.link.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;

/**
 * A helper that allows to setup (potentially nested) EntityConfig in a fluent
 * manner.
 * <p>
 * Thread safety note: EntityConfigBuilder can be applied in parallel from
 * multiple threads. All the configuration methods should be considered
 * thread-unsafe. In other words it should be configured once and then reused if
 * needed.
 * 
 * @since 1.2
 */
// TODO: java 8 lambdas should make this obsolete I guess?
public class EntityConfigBuilder {

	private Collection<Runnable> ops;
	private ThreadLocal<EntityConfig> config;

	public static EntityConfigBuilder config() {
		return new EntityConfigBuilder();
	}

	public static EntityConfigBuilder emptyConfig() {
		return config().excludeAttributes().excludeChildren();
	}

	private EntityConfigBuilder() {
		this.ops = new ArrayList<>();
		this.config = new ThreadLocal<>();
	}

	public void apply(EntityConfig config) {

		// since EntityConfigBuilder is reusable across threads, we must take
		// care to avoid race conditions on apply...

		this.config.set(config);

		try {
			for (Runnable op : ops) {
				op.run();
			}
		} finally {
			this.config.set(null);
		}
	}

	private EntityConfig getConfig() {
		return config.get();
	}

	/**
	 * Excludes all previously included attributes.
	 */
	public EntityConfigBuilder excludeAttributes() {

		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().excludeAttributes();
			}
		});

		return this;
	}

	/**
	 * Excludes all previously included child configs.
	 */
	public EntityConfigBuilder excludeChildren() {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().excludeChildren();
			}
		});

		return this;
	}

	public EntityConfigBuilder attribute(final String attribute) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().attribute(attribute);
			}
		});

		return this;
	}

	public EntityConfigBuilder attribute(final Property<?> attribute) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().attribute(attribute);
			}
		});
		return this;
	}

	public EntityConfigBuilder attributes(final Property<?>... attributes) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().attributes(attributes);
			}
		});
		return this;
	}

	public EntityConfigBuilder attributes(final String... attributes) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().attributes(attributes);
			}
		});

		return this;
	}

	public EntityConfigBuilder includeId(final boolean include) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().includeId(include);
			}
		});
		return this;
	}

	public EntityConfigBuilder includeId() {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().includeId();
			}
		});
		return this;
	}

	public EntityConfigBuilder excludeId() {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().excludeId();
			}
		});

		return this;
	}

	public EntityConfigBuilder and(final Expression qualifier) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().and(qualifier);
			}
		});
		return this;
	}

	public EntityConfigBuilder or(final Expression qualifier) {
		ops.add(new Runnable() {

			@Override
			public void run() {
				getConfig().or(qualifier);
			}
		});

		return this;
	}

	public EntityConfigBuilder path(final Property<?> path, final EntityConfigBuilder subentityBuilder) {

		ops.add(new Runnable() {

			@Override
			public void run() {
				subentityBuilder.apply(getConfig().path(path));
			}
		});

		return this;
	}

	public EntityConfigBuilder path(final String path, final EntityConfigBuilder subentityBuilder) {

		ops.add(new Runnable() {

			@Override
			public void run() {
				subentityBuilder.apply(getConfig().path(path));
			}
		});

		return this;
	}
}
