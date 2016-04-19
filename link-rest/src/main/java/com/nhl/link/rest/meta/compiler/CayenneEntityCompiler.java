package com.nhl.link.rest.meta.compiler;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.cayenne.CayenneLrAttribute;
import com.nhl.link.rest.meta.cayenne.CayenneLrDbAttribute;
import com.nhl.link.rest.meta.cayenne.CayenneLrEntity;
import com.nhl.link.rest.meta.cayenne.CayenneLrRelationship;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

/**
 * @since 1.24
 */
public class CayenneEntityCompiler implements LrEntityCompiler {

	/**
	 * A DI key that allows to expand the model of persistent entities coming
	 * from Cayenne.
	 * 
	 * @since 1.12
	 */
	public static final String ENTITY_OVERLAY_MAP = "linkrest.meta.entity.overlay.map";

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
				throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, "Unknown class: " + typeName, e);
			}
		}
		}
	}

	private EntityResolver resolver;
	private Map<String, LrEntityOverlay<?>> entityOverlays;

	public CayenneEntityCompiler(@Inject ICayennePersister cayennePersister,
			@Inject(ENTITY_OVERLAY_MAP) Map<String, LrEntityOverlay<?>> entityOverlays) {
		this.resolver = cayennePersister.entityResolver();
		this.entityOverlays = entityOverlays;
	}

	@Override
	public <T> LrEntity<T> compile(Class<T> type) {

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
			LrAttribute id = attribute != null
					? new CayenneLrAttribute(attribute, getJavaTypeForTypeName(attribute.getType()))
					: new CayenneLrDbAttribute(pk.getName(), pk,
							getJavaTypeForTypeName(TypesMapping.getJavaBySqlType(pk.getType())));
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
}
