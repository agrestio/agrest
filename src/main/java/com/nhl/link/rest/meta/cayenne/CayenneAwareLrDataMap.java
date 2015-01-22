package com.nhl.link.rest.meta.cayenne;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.runtime.parser.PathConstants;

/**
 * An {@link LrDataMap} that can resolve metadata from Cayenne mapping,
 * combining it with application-provided entities and metadata extensions.
 * 
 * @since 1.12
 */
public class CayenneAwareLrDataMap implements LrDataMap {

	private EntityResolver resolver;
	private ConcurrentMap<Class<?>, LrEntity<?>> entities;
	private Map<String, LrEntityOverlay<?>> entityOverlays;

	public CayenneAwareLrDataMap(EntityResolver resolver, List<LrEntity<?>> extraEntities,
			Map<String, LrEntityOverlay<?>> entityOverlays) {

		this.resolver = resolver;
		this.entityOverlays = entityOverlays;
		this.entities = new ConcurrentHashMap<>();

		for (LrEntity<?> e : extraEntities) {
			entities.put(e.getType(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LrEntity<T> getEntity(Class<T> type) {
		LrEntity<?> e = entities.get(type);

		// lazily create Cayenne-based entities, improving startup time
		// and decreasing memory footprint.
		if (e == null) {

			LrEntity<?> newEntity = createCayenneEntity(type);
			LrEntity<?> existingEntity = entities.putIfAbsent(type, newEntity);
			e = existingEntity != null ? existingEntity : newEntity;
		}

		return (LrEntity<T>) e;
	}

	private <T> LrPersistentEntity<T> createCayenneEntity(Class<T> type) {

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

		for (DbAttribute pk : objEntity.getDbEntity().getPrimaryKeys()) {
			ObjAttribute attribute = objEntity.getAttributeForDbAttribute(pk);
			LrAttribute id = attribute != null ? new CayenneLrAttribute(attribute) : new CayenneLrDbAttribute(
					PathConstants.ID_PK_ATTRIBUTE, pk);
			lrEntity.addId(id);
		}

		LrEntityOverlay<?> overlay = entityOverlays.get(type.getName());
		if (overlay != null) {

			for (String a : overlay.getTransientAttributes()) {
				// TODO: figure out the type
				DefaultLrAttribute lrAttribute = new DefaultLrAttribute(a, "java.lang.Object");
				lrEntity.addAttribute(lrAttribute);
			}
		}

		return lrEntity;
	}

}
