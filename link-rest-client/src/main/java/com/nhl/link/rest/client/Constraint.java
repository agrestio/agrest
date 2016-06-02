package com.nhl.link.rest.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cayenne.exp.Expression;

/**
 * @since 2.0
 */
class Constraint {

	private String mapByPath;
	private Expression cayenneExp;
	private Map<String, Sort> orderingMap;
	private Long startIndex;
	private Long limit;

	private Set<String> excludes;
	private Map<String, Include> includeMap;

	public Constraint mapBy(String mapByPath) {
		this.mapByPath = mapByPath;
		return this;
	}

	public Constraint cayenneExp(Expression cayenneExp) {
		this.cayenneExp = cayenneExp;
		return this;
	}

	public Constraint sort(String... properties) {

		if (properties != null) {
			for (String property : properties) {
				addSort(property, Sort.property(property));
			}
		}
		return this;
	}

	public Constraint sort(Sort ordering) {

		if (ordering != null) {
			addSort(ordering.getPropertyName(), ordering);
		}
		return this;
	}

	private void addSort(String property, Sort sort) {

		if (orderingMap == null) {
			orderingMap = new HashMap<>();
		}
		orderingMap.put(property, sort);
	}

	public Constraint start(long startIndex) {

		if (startIndex >= 0) {
			this.startIndex = startIndex;
		}
		return this;
	}

	public Constraint limit(long limit) {

		if (limit > 0) {
			this.limit = limit;
		}
		return this;
	}

	public Constraint exclude(String... excludePaths) {

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
		return this;
	}

	public Constraint include(String... includePaths) {

		if (includePaths != null) {
			for (String includePath : includePaths) {
				if (includePath != null) {
					addInclude(includePath, Include.path(includePath));
				}
			}
		}
		return this;
	}

	public Constraint include(Include include) {

		if (include != null) {
			addInclude(include.getPath(), include);
		}
		return this;
	}

	private void addInclude(String path, Include include) {

		if (includeMap == null) {
			includeMap = new HashMap<>();
		}
		includeMap.put(path, include);
	}

	public boolean hasAnyConstraints() {
		return mapByPath != null || cayenneExp != null || startIndex != null || limit != null
				|| (orderingMap != null && orderingMap.size() > 0) || (excludes != null && excludes.size() > 0)
				|| (includeMap != null && includeMap.size() > 0);
	}

	String getMapBy() {
		return mapByPath;
	}

	Expression getCayenneExp() {
		return cayenneExp;
	}

	Collection<Sort> getOrderings() {
		return orderingMap == null ? Collections.emptyList() : orderingMap.values();
	}

	Long getStart() {
		return startIndex;
	}

	Long getLimit() {
		return limit;
	}

	Collection<String> getExcludes() {
		return excludes == null ? Collections.emptyList() : excludes;
	}

	Collection<Include> getIncludes() {
		return includeMap == null ? Collections.emptyList() : includeMap.values();
	}
}
