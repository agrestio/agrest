package com.nhl.link.rest.client.protocol;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.cayenne.exp.Expression;

/**
 * A base superlcass of representations of either a root entity request or an
 * entity subrequest.
 * 
 * @since 2.0
 */
public abstract class LrcEntityRequest {

	protected Expression cayenneExp;
	protected Map<String, Sort> orderingMap;
	protected long start;
	protected long limit;

	public Optional<Expression> getCayenneExp() {
		return Optional.ofNullable(cayenneExp);
	}

	void setCayenneExp(Expression cayenneExp) {
		this.cayenneExp = cayenneExp;
	}

	public Collection<Sort> getOrderings() {
		return orderingMap == null ? Collections.emptyList() : orderingMap.values();
	}

	void addOrderings(Sort... orderings) {

		if (orderings != null) {
			for (Sort o : orderings) {
				addOrdering(o.getPropertyName(), o);
			}
		}
	}

	private void addOrdering(String property, Sort sort) {

		if (orderingMap == null) {
			orderingMap = new HashMap<>();
		}
		orderingMap.put(property, sort);
	}

	public Optional<Long> getLimit() {
		return limit > 0 ? Optional.of(limit) : Optional.empty();
	}

	void setLimit(long limit) {
		this.limit = limit;
	}

	public Optional<Long> getStart() {
		return start > 0 ? Optional.of(start) : Optional.empty();
	}

	void setStart(long start) {
		this.start = start;
	}
}
