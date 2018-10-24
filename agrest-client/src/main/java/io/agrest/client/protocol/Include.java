package io.agrest.client.protocol;

import java.util.Objects;
import java.util.Optional;

/**
 * @since 2.0
 */
public class Include extends AgcEntityRequest {

	public static IncludeBuilder path(String path) {
		return new IncludeBuilder(path);
	}

	private String path;

	// TODO: for now Agrest only supports 'mapBy' in nested includes. Once it is
	// supported at the root level this will have to be moved to the superclass
	protected String mapBy;

	protected Include(String path) {
		this.path = Objects.requireNonNull(path);
	}

	/**
	 * Returns true if the include is simply a path and doesn't have any
	 * addition constraints.
	 * 
	 * @return true if the include is simply a path and doesn't have any
	 *         addition constraints.
	 */
	public boolean isSimple() {
		return mapBy == null && cayenneExp == null && start <= 0 && limit <= 0
				&& (orderingMap == null || orderingMap.isEmpty());
	}

	public String getPath() {
		return path;
	}

	public Optional<String> getMapBy() {
		return Optional.ofNullable(mapBy);
	}

	protected void setMapBy(String mapBy) {
		this.mapBy = mapBy;
	}

	public static class IncludeBuilder {

		private Include include;

		private IncludeBuilder(String path) {
			this.include = new Include(path);
		}

		public Include build() {
			return include;
		}

		public IncludeBuilder mapBy(String mapByPath) {
			include.setMapBy(mapByPath);
			return this;
		}

		public IncludeBuilder start(long start) {
			include.setStart(start);
			return this;
		}

		public IncludeBuilder limit(long limit) {
			include.setLimit(limit);
			return this;
		}

		public IncludeBuilder cayenneExp(Expression.ExpressionBuilder exp) {
			include.setCayenneExp(exp.build());
			return this;
		}

		public IncludeBuilder sort(String... properties) {

			if (properties != null) {

				Sort[] orderings = new Sort[properties.length];

				for (int i = 0; i < properties.length; i++) {
					orderings[i] = Sort.property(properties[i]);
				}

				include.addOrderings(orderings);
			}

			return this;
		}

		public IncludeBuilder sort(Sort... properties) {
			include.addOrderings(properties);
			return this;
		}
	}
}
