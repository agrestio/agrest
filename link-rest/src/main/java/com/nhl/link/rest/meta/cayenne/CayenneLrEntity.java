package com.nhl.link.rest.meta.cayenne;

import com.nhl.link.rest.meta.DefaultLrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.12
 */
public class CayenneLrEntity<T> extends DefaultLrEntity<T> implements LrPersistentEntity<T> {

	private Map<String, LrPersistentAttribute> persistentAttributes;

	public CayenneLrEntity(Class<T> type, String name) {
		super(type, name);
		this.persistentAttributes = new HashMap<>();
	}

	@Override
	public LrPersistentAttribute getPersistentAttribute(String name) {
		return persistentAttributes.get(name);
	}

	@Override
	public Collection<LrPersistentAttribute> getPersistentAttributes() {
		return persistentAttributes.values();
	}

	public void addPersistentAttribute(LrPersistentAttribute attribute) {
		persistentAttributes.put(attribute.getName(), attribute);
		super.addAttribute(attribute);
	}
}
