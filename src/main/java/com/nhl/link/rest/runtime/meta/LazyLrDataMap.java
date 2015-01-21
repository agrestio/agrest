package com.nhl.link.rest.runtime.meta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;

/**
 * A {@link LrDataMap} that lazily creates its entities, improving startup time
 * and decreasing memory footprint. Memory savings come from the fact that the
 * entire Cayenne mapping does not need to be converted to {@link LrDataMap}
 * immediately.
 * 
 * @since 1.12
 */
public class LazyLrDataMap implements LrDataMap {

	private EntityResolver resolver;
	private ConcurrentMap<Class<?>, LrEntity<?>> entities;
	private Map<String, LrEntityOverlay<?>> entityOverlays;

	public LazyLrDataMap(EntityResolver resolver, Map<String, LrEntityOverlay<?>> entityOverlays) {
		this.resolver = resolver;
		this.entities = new ConcurrentHashMap<>();
		this.entityOverlays = entityOverlays;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LrEntity<T> getEntity(Class<T> type) {
		LrEntity<?> e = entities.get(type);

		if (e == null) {

			LrEntity<?> newEntity = createEntity(type);
			LrEntity<?> existingEntity = entities.putIfAbsent(type, newEntity);
			e = existingEntity != null ? existingEntity : newEntity;
		}

		return (LrEntity<T>) e;
	}

	private <T> LrEntity<T> createEntity(Class<T> type) {

		ObjEntity objEntity = resolver.getObjEntity(type);
		if (objEntity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		CayenneLrEntity<T> lrEntity = new CayenneLrEntity<T>(type, objEntity);

		for (ObjAttribute a : objEntity.getAttributes()) {
			CayenneLrAttribute lrAttribute = new CayenneLrAttribute(a);
			lrEntity.addPersistentAttribute(lrAttribute);
		}

		for (ObjRelationship r : objEntity.getRelationships()) {

			Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();
			CayenneLrRelationship lrRelationship = new CayenneLrRelationship(r, targetEntityType);
			lrEntity.addRelationship(lrRelationship);
		}

		LrEntityOverlay<?> overlay = entityOverlays.get(type.getName());
		if (overlay != null) {

			for (String a : overlay.getTransientAttributes()) {
				// TODO: figure out the type
				DefaultLrAttribute lrAttribute = new DefaultLrAttribute(a, "java.lang.Object");
				lrEntity.addTransientAttribute(lrAttribute);
			}
		}

		return lrEntity;
	}

}
