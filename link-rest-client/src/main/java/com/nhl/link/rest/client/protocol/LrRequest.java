package com.nhl.link.rest.client.protocol;

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
public class LrRequest {

	private String mapByPath;
	private Expression cayenneExp;
	private Map<String, Sort> orderingMap;
	private Long startIndex;
	private Long limit;

	private Set<String> excludes;
	private Map<String, Include> includeMap;

	public LrRequest mapBy(String mapByPath) {
		this.mapByPath = mapByPath;
		return this;
	}

	public LrRequest cayenneExp(Expression cayenneExp) {
		this.cayenneExp = cayenneExp;
		return this;
	}

	public LrRequest sort(String... properties) {

		if (properties != null) {
			for (String property : properties) {
				addSort(property, Sort.property(property));
			}
		}
		return this;
	}

	public LrRequest sort(Sort ordering) {

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

	public LrRequest start(long startIndex) {

		if (startIndex >= 0) {
			this.startIndex = startIndex;
		}
		return this;
	}

	public LrRequest limit(long limit) {

		if (limit > 0) {
			this.limit = limit;
		}
		return this;
	}

	public LrRequest exclude(String... excludePaths) {

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

	public LrRequest include(String... includePaths) {

		if (includePaths != null) {
			for (String includePath : includePaths) {
				if (includePath != null) {
					addInclude(includePath, Include.path(includePath));
				}
			}
		}
		return this;
	}

	public LrRequest include(Include include) {

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

	public String getMapBy() {
		return mapByPath;
	}

	public Expression getCayenneExp() {
		return cayenneExp;
	}

	public Collection<Sort> getOrderings() {
		return orderingMap == null ? Collections.emptyList() : orderingMap.values();
	}

	public Long getStart() {
		return startIndex;
	}

	public Long getLimit() {
		return limit;
	}

	public Collection<String> getExcludes() {
		return excludes == null ? Collections.emptyList() : excludes;
	}

	public Collection<Include> getIncludes() {
		return includeMap == null ? Collections.emptyList() : includeMap.values();
	}
}
