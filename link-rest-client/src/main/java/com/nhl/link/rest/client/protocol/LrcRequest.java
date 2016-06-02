package com.nhl.link.rest.client.protocol;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.client.protocol.Include.IncludeBuilder;

/**
 * @since 2.0
 */
public class LrcRequest extends LrcEntityRequest {

	private Set<String> excludes;
	private Map<String, Include> includeMap;

	public static LrRequestBuilder builder() {
		return new LrRequestBuilder();
	}

	protected LrcRequest() {
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

	public static class LrRequestBuilder {

		private LrcRequest request;

		private LrRequestBuilder() {
			this.request = new LrcRequest();
		}

		public LrcRequest build() {
			return request;
		}

		public LrRequestBuilder exclude(String... excludePaths) {
			request.addExcludes(excludePaths);
			return this;
		}

		public LrRequestBuilder include(String... includePaths) {
			if (includePaths != null) {
				Include[] includes = new Include[includePaths.length];
				for (int i = 0; i < includePaths.length; i++) {
					includes[i] = Include.path(includePaths[i]).build();
				}

				request.addIncludes(includes);
			}

			return this;
		}

		public LrRequestBuilder include(Include include) {
			request.addIncludes(include);
			return this;
		}

		public LrRequestBuilder include(IncludeBuilder include) {
			request.addIncludes(include.build());
			return this;
		}

		public LrRequestBuilder start(long start) {
			request.setStart(start);
			return this;
		}

		public LrRequestBuilder limit(long limit) {
			request.setLimit(limit);
			return this;
		}

		public LrRequestBuilder cayenneExp(Expression exp) {
			request.setCayenneExp(exp);
			return this;
		}

		public LrRequestBuilder sort(String... properties) {

			if (properties != null) {

				Sort[] orderings = new Sort[properties.length];

				for (int i = 0; i < properties.length; i++) {
					orderings[i] = Sort.property(properties[i]);
				}

				request.addOrderings(orderings);
			}

			return this;
		}

		public LrRequestBuilder sort(Sort... properties) {
			request.addOrderings(properties);
			return this;
		}
	}
}
