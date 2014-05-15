package com.nhl.link.rest.runtime.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;

class PojoEntity extends ObjEntity {

	private static final long serialVersionUID = 5485635014494337551L;

	private Map<String, ObjAttribute> pk;

	public PojoEntity(String name) {
		super(name);
		this.pk = new HashMap<String, ObjAttribute>();
	}

	public void addPrimaryKey(ObjAttribute a) {
		pk.put(a.getName(), a);
	}

	@Override
	public Collection<String> getPrimaryKeyNames() {
		return pk.keySet();
	}

	@Override
	public Collection<ObjAttribute> getPrimaryKeys() {
		return pk.values();
	}
}
