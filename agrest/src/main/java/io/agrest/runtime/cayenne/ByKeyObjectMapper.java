package io.agrest.runtime.cayenne;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import org.apache.cayenne.DataObject;
import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.ExpressionFactory;

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
