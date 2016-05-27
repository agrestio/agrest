package com.nhl.link.rest;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.parser.converter.Normalizer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A single value id.
 * 
 * @since 1.24
 */
public class SimpleObjectId extends BaseObjectId {

	private Object id;

	public SimpleObjectId(Object id) {
		super();
		this.id = id;
	}

	@Override
	public Object get(String attributeName) {
		return get();
	}

	@Override
	public Object get() {
		return id;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	protected Map<String, Object> asMap(Collection<LrAttribute> idAttributes) {
		LrAttribute idAttribute = idAttributes.iterator().next();
		return Collections.singletonMap(idAttribute.getName(), Normalizer.normalize(id, idAttribute.getType()));
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
