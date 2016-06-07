package com.nhl.link.rest.meta.cayenne;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.compiler.CompilerContext;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Map;

/**
 * @since 1.24
 */
public class CayenneEntityCompiler implements LrEntityCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CayenneEntityCompiler.class);

	/**
	 * A DI key that allows to expand the model of persistent entities coming
	 * from Cayenne.
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
	public <T> LrEntity<T> compile(Class<T> type, CompilerContext compilerContext) {

		ObjEntity objEntity = resolver.getObjEntity(type);
		if (objEntity == null) {
			return null;
		}

		LOGGER.debug("compiling Cayenne entity for type: " + type);

		CayenneLrEntity<T> lrEntity =
				(CayenneLrEntity<T>) compilerContext.addEntityIfAbsent(type, new CayenneLrEntity<>(type, objEntity));
		loadCayenneEntity(lrEntity, compilerContext);
		loadAnnotatedProperties(lrEntity, compilerContext);
		loadOverlays(lrEntity);
		return lrEntity;
	}

	protected <T> void loadCayenneEntity(CayenneLrEntity<T> lrEntity, CompilerContext compilerContext) {

		ObjEntity objEntity = lrEntity.getObjEntity();
		for (ObjAttribute a : objEntity.getAttributes()) {
			CayenneLrAttribute lrAttribute = new CayenneLrAttribute(a, getJavaTypeForTypeName(a.getType()));
			lrEntity.addPersistentAttribute(lrAttribute);
		}

		for (ObjRelationship r : objEntity.getRelationships()) {

			Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();

			LrEntity<?> targetEntity = compilerContext.getOrCreateEntity(targetEntityType);
			CayenneLrRelationship lrRelationship = new CayenneLrRelationship(r, targetEntity);
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

	}

	protected <T> void loadAnnotatedProperties(CayenneLrEntity<T> entity, CompilerContext compilerContext) {

		// load a separate entity built purely from annotations, then merge it
		// with our entity... Note that we are not cloning attributes or
		// relationship during merge... they have no references to parent and
		// can be used as is.

		LrEntity<T> annotatedEntity = LrEntityBuilder.build(entity.getType(), compilerContext);

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
