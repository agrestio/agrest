package com.nhl.link.rest.meta.cayenne;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.compiler.LazyLrPersistentEntity;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since 1.24
 */
public class CayenneEntityCompiler implements LrEntityCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CayenneEntityCompiler.class);

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
	private Map<String, LrEntityOverlay> entityOverlays;
	private IJsonValueConverterFactory converterFactory;

	public CayenneEntityCompiler(@Inject ICayennePersister cayennePersister,
								 @Inject Map<String, LrEntityOverlay> entityOverlays,
								 @Inject IJsonValueConverterFactory converterFactory) {
		this.resolver = cayennePersister.entityResolver();
		this.entityOverlays = entityOverlays;
		this.converterFactory = converterFactory;
	}

	@Override
	public <T> LrEntity<T> compile(Class<T> type, LrDataMap dataMap) {

		ObjEntity objEntity = resolver.getObjEntity(type);
		if (objEntity == null) {
			return null;
		}
		return new LazyLrPersistentEntity<>(type, () -> doCompile(type, dataMap));
	}

	private <T> LrPersistentEntity<T> doCompile(Class<T> type, LrDataMap dataMap) {

		LOGGER.debug("compiling Cayenne entity for type: " + type);

		ObjEntity objEntity = resolver.getObjEntity(type);
		CayenneLrEntity<T> lrEntity = new CayenneLrEntity<>(type, objEntity);
		loadCayenneEntity(lrEntity, dataMap);
		loadAnnotatedProperties(lrEntity, dataMap);
		loadOverlays(lrEntity);
		return lrEntity;
	}

	protected <T> void loadCayenneEntity(CayenneLrEntity<T> lrEntity, LrDataMap dataMap) {

		ObjEntity objEntity = lrEntity.getObjEntity();
		for (ObjAttribute a : objEntity.getAttributes()) {
			Class<?> type = getJavaTypeForTypeName(a.getType());
			CayenneLrAttribute lrAttribute = new CayenneLrAttribute(a, type);
			lrEntity.addPersistentAttribute(lrAttribute);
		}

		for (ObjRelationship r : objEntity.getRelationships()) {
			List<DbRelationship> dbRelationshipsList = r.getDbRelationships();

			Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();
			LrEntity<?> targetEntity = dataMap.getEntity(targetEntityType);

			// take last element from list of db relationships
			// in order to behave correctly if
			// db entities are connected through intermediate tables
			DbRelationship targetRelationship = dbRelationshipsList.get(dbRelationshipsList.size() - 1);
			int targetJdbcType = targetRelationship.getJoins().get(0).getTarget().getType();
			Class<?> type = getJavaTypeForTypeName(TypesMapping.getJavaBySqlType(targetJdbcType));

			LrRelationship lrRelationship = new CayenneLrRelationship(r, targetEntity, converterFactory.converter(type));
			lrEntity.addRelationship(lrRelationship);
		}

		for (DbAttribute pk : objEntity.getDbEntity().getPrimaryKeys()) {
			ObjAttribute attribute = objEntity.getAttributeForDbAttribute(pk);
			Class<?> type;
			LrAttribute id;
			if (attribute == null) {
				type = getJavaTypeForTypeName(TypesMapping.getJavaBySqlType(pk.getType()));
				id = new CayenneLrDbAttribute(pk.getName(), pk, type);
			} else {
				type = getJavaTypeForTypeName(attribute.getType());
				id = new CayenneLrAttribute(attribute, type);
			}
			lrEntity.addId(id);
		}

	}

	protected <T> void loadAnnotatedProperties(CayenneLrEntity<T> entity, LrDataMap dataMap) {

		// load a separate entity built purely from annotations, then merge it
		// with our entity... Note that we are not cloning attributes or
		// relationship during merge... they have no references to parent and
		// can be used as is.

		LrEntity<T> annotatedEntity = new LrEntityBuilder<>(entity.getType(), dataMap, converterFactory).build();

		if (annotatedEntity.getIds().size() > 0) {
			for (LrAttribute id : annotatedEntity.getIds()) {

				LrAttribute existing = entity.addId(id);
				if (existing != null && LOGGER.isDebugEnabled()) {
					LOGGER.debug("ID attribute '" + existing.getName() + "' is overridden from annotations.");
				}
			}

			Iterator<LrAttribute> iter = entity.getIds().iterator();
			while (iter.hasNext()) {
				LrAttribute id = iter.next();
				if (!annotatedEntity.getIds().contains(id)) {
					iter.remove();
				}
			}
		}

		for (LrAttribute attribute : annotatedEntity.getAttributes()) {

			LrAttribute existing = entity.addAttribute(attribute);
			if (existing != null && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Attribute '" + existing.getName() + "' is overridden from annotations.");
			}
		}

		for (LrRelationship relationship : annotatedEntity.getRelationships()) {

			LrRelationship existing = entity.addRelationship(relationship);
			if (existing != null && LOGGER.isDebugEnabled()) {
				LOGGER.debug("Relationship '" + existing.getName() + "' is overridden from annotations.");
			}
		}
	}

	protected <T> void loadOverlays(CayenneLrEntity<T> entity) {
		LrEntityOverlay<?> overlay = entityOverlays.get(entity.getType().getName());
		if (overlay != null) {

			for (String a : overlay.getTransientAttributes()) {
				// TODO: figure out the type
				DefaultLrAttribute lrAttribute = new DefaultLrAttribute(a, Object.class);
				entity.addAttribute(lrAttribute);
			}
		}
	}

}
