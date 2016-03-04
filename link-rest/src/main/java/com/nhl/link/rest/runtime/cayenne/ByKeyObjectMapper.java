package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;

/**
 * @since 1.4
 */
class ByKeyObjectMapper<T> implements ObjectMapper<T> {

	private String keyProperty;

	ByKeyObjectMapper(String keyProperty) {
		this.keyProperty = keyProperty;
	}

	@Override
	public Object keyForObject(T object) {
		return ((DataObject) object).readProperty(keyProperty);
	}

	@Override
	public Object keyForUpdate(EntityUpdate<T> u) {
		return u.getValues().get(keyProperty);
	}

	@Override
	public Expression expressionForKey(Object key) {
		// allowing nulls here
		return ExpressionFactory.matchExp(keyProperty, key);
	}

}
