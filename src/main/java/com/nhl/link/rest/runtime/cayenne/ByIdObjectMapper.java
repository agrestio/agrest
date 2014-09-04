package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTPath;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;

/**
 * @since 1.7
 */
class ByIdObjectMapper<T> implements ObjectMapper<T> {

	private ASTPath keyPath;

	ByIdObjectMapper(ASTPath keyPath) {
		// this can be a "db:" or "obj:" expression, so treating it as an opaque
		// Expression, letting Cayenne to figure out the difference
		this.keyPath = keyPath;
	}

	@Override
	public Expression expressionForKey(Object key) {

		// can't match by NULL id
		if (key == null) {
			return null;
		}

		return new ASTEqual(keyPath, key);
	}

	@Override
	public Object keyForObject(T object) {
		return Cayenne.pkForObject((Persistent) object);
	}

	@Override
	public Object keyForUpdate(EntityUpdate update) {
		return update.getId();
	}

}
