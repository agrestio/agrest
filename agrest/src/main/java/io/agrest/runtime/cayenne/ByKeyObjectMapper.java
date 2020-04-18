package io.agrest.runtime.cayenne;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.meta.AgAttribute;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTEqual;

class ByKeyObjectMapper<T> implements ObjectMapper<T> {

	private AgAttribute attribute;

	public ByKeyObjectMapper(AgAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public Object keyForObject(T object) {
		return attribute.getPropertyReader().value(object, attribute.getName());
	}

	@Override
	public Object keyForUpdate(EntityUpdate<T> u) {
		return u.getValues().get(attribute.getName());
	}

	@Override
	public Expression expressionForKey(Object key) {
		// allowing nulls here
		return new ASTEqual(attribute.getPathExp(), key);
	}
}
