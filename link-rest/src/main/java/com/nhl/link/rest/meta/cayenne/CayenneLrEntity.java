package com.nhl.link.rest.meta.cayenne;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.meta.DefaultLrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.LrPersistentEntity;

/**
 * @since 1.12
 */
public class CayenneLrEntity<T> extends DefaultLrEntity<T> implements LrPersistentEntity<T> {

	private ObjEntity objEntity;
	private Map<String, LrPersistentAttribute> persistentAttributes;

	public CayenneLrEntity(Class<T> type, ObjEntity objEntity) {
		super(type);
		this.objEntity = objEntity;
		this.persistentAttributes = new HashMap<>();
	}

	@Override
	public String getName() {
		return objEntity.getName();
	}

	@Override
	public ObjEntity getObjEntity() {
		return objEntity;
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
