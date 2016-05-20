package com.nhl.link.rest.runtime.cayenne;

import static org.apache.cayenne.exp.ExpressionFactory.joinExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.property.DataObjectPropertyReader;
import com.nhl.link.rest.property.PersistentObjectIdPropertyReader;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTPath;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;

/**
 * @since 1.7
 */
class ByIdObjectMapper<T> implements ObjectMapper<T> {

	private ASTPath[] keyPaths;

	ByIdObjectMapper(ASTPath[] keyPaths) {
		// this can be a "db:" or "obj:" expression, so treating it as an opaque
		// Expression, letting Cayenne to figure out the difference
		this.keyPaths = keyPaths;
	}

	@Override
	public Expression expressionForKey(Object key) {

		// can't match by NULL id
		if (key == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> idMap = (Map<String, Object>) key;

		// can't match by NULL id
		if (idMap.isEmpty()) {
			return null;
		}

		int len = keyPaths.length;
		if (len == 1) {
			return match(keyPaths[0], idMap);
		}

		List<Expression> exps = new ArrayList<>(len);
		for (ASTPath p : keyPaths) {
			exps.add(match(p, idMap));
		}
		return joinExp(Expression.AND, exps);
	}

	private Expression match(ASTPath path, Map<String, Object> idMap) {

		Object value = idMap.get(path.getPath());
		if (value == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No ID value for path: " + path);
		}

		return new ASTEqual(path, value);
	}

	@Override
	public Object keyForObject(T object) {
		Map<String, Object> idMap = new HashMap<>();
		for (ASTPath keyPath : keyPaths) {
			idMap.put(keyPath.getPath(), readPropertyOrId(object, keyPath.getPath()));
		}
		return idMap;
	}

	private Object readPropertyOrId(Object object, String name) {
		// try normal property first, and if it's absent,
		// assume that it's (a part of) the entity's ID
		Object property = DataObjectPropertyReader.reader().value(object, name);
		return property == null ?
				PersistentObjectIdPropertyReader.reader().value(object, name) : property;
    }

	@Override
	public Object keyForUpdate(EntityUpdate<T> update) {
		return update.getId();
	}

}
