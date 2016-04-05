package com.nhl.link.rest.meta.cayenne;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.dba.TypesMapping;
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
			CayenneLrAttribute lrAttribute = new CayenneLrAttribute(a, getJavaTypeForTypeName(a.getType()));
			lrEntity.addPersistentAttribute(lrAttribute);
		}

		for (ObjRelationship r : objEntity.getRelationships()) {

			Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();
			CayenneLrRelationship lrRelationship = new CayenneLrRelationship(r, targetEntityType);
			lrEntity.addRelationship(lrRelationship);
		}

		for (DbAttribute pk : objEntity.getDbEntity().getPrimaryKeys()) {
			ObjAttribute attribute = objEntity.getAttributeForDbAttribute(pk);
			LrAttribute id = attribute != null ? new CayenneLrAttribute(attribute, getJavaTypeForTypeName(attribute.getType()))
					: new CayenneLrDbAttribute(pk.getName(), pk, getJavaTypeForTypeName(TypesMapping.getJavaBySqlType(pk.getType())));
			lrEntity.addId(id);
		}

		LrEntityOverlay<?> overlay = entityOverlays.get(type.getName());
		if (overlay != null) {

			for (String a : overlay.getTransientAttributes()) {
				// TODO: figure out the type
				DefaultLrAttribute lrAttribute = new DefaultLrAttribute(a, Object.class);
				lrEntity.addAttribute(lrAttribute);
			}
		}

		return lrEntity;
	}

	static Class<?> getJavaTypeForTypeName(String typeName) {

		if (typeName == null) {
			throw new NullPointerException("Attribute type cannot be null");
		}

		switch (typeName) {
			case TypesMapping.JAVA_BYTES:
				return byte[].class;
			case "boolean":
				return boolean.class;
			case "byte":
				return byte.class;
			case "char":
				return char.class;
			case "short":
				return short.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			default: {
				try {
					return Class.forName(typeName);
				} catch (ClassNotFoundException e) {
					throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR,
							"Unknown class: " + typeName, e);
				}
			}
		}
	}

}
