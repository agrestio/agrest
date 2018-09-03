package io.agrest.meta.cayenne;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.agrest.meta.AgPersistentEntity;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.AgPersistentAttribute;
import org.apache.cayenne.map.ObjEntity;

/**
 * @since 1.12
 */
public class CayenneAgEntity<T> extends DefaultAgEntity<T> implements AgPersistentEntity<T> {

	private ObjEntity objEntity;
	private Map<String, AgPersistentAttribute> persistentAttributes;

	public CayenneAgEntity(Class<T> type, ObjEntity objEntity) {
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
	public AgPersistentAttribute getPersistentAttribute(String name) {
		return persistentAttributes.get(name);
	}

	@Override
	public Collection<AgPersistentAttribute> getPersistentAttributes() {
		return persistentAttributes.values();
	}

	public void addPersistentAttribute(AgPersistentAttribute attribute) {
		persistentAttributes.put(attribute.getName(), attribute);
		super.addAttribute(attribute);
	}
}
