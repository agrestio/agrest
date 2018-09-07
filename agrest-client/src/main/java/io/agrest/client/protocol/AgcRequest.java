package io.agrest.client.protocol;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.0
 */
public class AgcRequest extends AgcEntityRequest {

	private Set<String> excludes;
	private Map<String, Include> includeMap;

	public static AgRequestBuilder builder() {
		return new AgRequestBuilder();
	}

	protected AgcRequest() {
	}

	protected void addExcludes(String... excludePaths) {

		if (excludePaths != null) {
			if (excludes == null) {
				excludes = new HashSet<>();
			}
			for (String excludePath : excludePaths) {
				if (excludePath != null) {
					excludes.add(excludePath);
				}
			}
		}
	}

	protected void addIncludes(Include... includes) {

		if (includes != null) {
			for (Include i : includes) {
				addInclude(i.getPath(), i);
			}
		}
	}

	protected void addInclude(String path, Include include) {

		if (includeMap == null) {
			includeMap = new HashMap<>();
		}
		includeMap.put(path, include);
	}

	public Collection<String> getExcludes() {
		return excludes == null ? Collections.emptyList() : excludes;
	}

	public Collection<Include> getIncludes() {
		return includeMap == null ? Collections.emptyList() : includeMap.values();
	}

	public static class AgRequestBuilder {

		private AgcRequest request;

		private AgRequestBuilder() {
			this.request = new AgcRequest();
		}

		public AgcRequest build() {
			return request;
		}

		public AgRequestBuilder exclude(String... excludePaths) {
			request.addExcludes(excludePaths);
			return this;
		}

		public AgRequestBuilder include(String... includePaths) {
			if (includePaths != null) {
				Include[] includes = new Include[includePaths.length];
				for (int i = 0; i < includePaths.length; i++) {
					includes[i] = Include.path(includePaths[i]).build();
				}

				request.addIncludes(includes);
			}

			return this;
		}

		public AgRequestBuilder include(Include include) {
			request.addIncludes(include);
			return this;
		}

		public AgRequestBuilder include(Include.IncludeBuilder include) {
			request.addIncludes(include.build());
			return this;
		}

		public AgRequestBuilder start(long start) {
			request.setStart(start);
			return this;
		}

		public AgRequestBuilder limit(long limit) {
			request.setLimit(limit);
			return this;
		}

		public AgRequestBuilder cayenneExp(Expression exp) {
			request.setCayenneExp(exp);
			return this;
		}

		public AgRequestBuilder sort(String... properties) {

			if (properties != null) {

				Sort[] orderings = new Sort[properties.length];

				for (int i = 0; i < properties.length; i++) {
					orderings[i] = Sort.property(properties[i]);
				}

				request.addOrderings(orderings);
			}

			return this;
		}

		public AgRequestBuilder sort(Sort... properties) {
			request.addOrderings(properties);
			return this;
		}
	}
}
