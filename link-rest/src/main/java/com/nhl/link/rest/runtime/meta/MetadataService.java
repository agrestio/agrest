package com.nhl.link.rest.runtime.meta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

public class MetadataService implements IMetadataService {

	/**
	 * A DI key that allows to access a list of entity compilers.
	 * 
	 * @since 1.24
	 */
	public static final String ENTITY_COMPILER_LIST = "linkrest.meta.entity.compiler.list";

	private EntityResolver entityResolver;
	private LrDataMap dataMap;

	public MetadataService(@Inject(ENTITY_COMPILER_LIST) List<LrEntityCompiler> entityCompilers,
			@Inject ICayennePersister cayenneService) {

		this.entityResolver = cayenneService.entityResolver();
		this.dataMap = new LazyLrDataMap(entityCompilers);
	}

	/**
	 * @since 1.12
	 */
	@Override
	public <T> LrEntity<T> getLrEntity(Class<T> type) {
		if (type == null) {
			throw new NullPointerException("Null type");
		}

		LrEntity<T> e = dataMap.getEntity(type);

		if (e == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid entity: " + type.getName());
		}

		return e;
	}

	/**
	 * @since 1.12
	 */
	@Override
	public <T> LrEntity<T> getLrEntity(Select<T> query) {
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) query.getMetaData(entityResolver).getClassDescriptor().getObjectClass();
		return getLrEntity(type);
	}

	/**
	 * @since 1.12
	 */
	@Override
	public LrRelationship getLrRelationship(Class<?> type, String relationship) {
		LrEntity<?> e = getLrEntity(type);
		LrRelationship r = e.getRelationship(relationship);
		if (r == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid relationship: '" + relationship + "'");
		}

		return r;
	}

	/**
	 * @since 1.12
	 */
	@Override
	public LrRelationship getLrRelationship(EntityParent<?> parent) {
		return getLrRelationship(parent.getType(), parent.getRelationship());
	}

	@Override
	public <T> LrEntity<T> getEntityByType(Type entityType) {
		@SuppressWarnings("unchecked")
		LrEntity<T> entity = getLrEntity( (Class<T>) entityTypeForParamType(entityType));
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"EntityUpdate type '" + entityType.getTypeName() + "' is not an entity");
		}
		return entity;
	}

	// TODO: duplication of code with ListenerInvocationFactory
	Class<?> entityTypeForParamType(Type paramType) {

		if (paramType instanceof ParameterizedType) {

			// the algorithm below is not universal. It doesn't check multiple
			// bounds...

			Type[] typeArgs = ((ParameterizedType) paramType).getActualTypeArguments();
			if (typeArgs.length == 1) {
				if (typeArgs[0] instanceof Class) {
					return (Class<?>) typeArgs[0];
				} else if (typeArgs[0] instanceof WildcardType) {
					Type[] upperBounds = ((WildcardType) typeArgs[0]).getUpperBounds();
					if (upperBounds.length == 1) {
						if (upperBounds[0] instanceof Class) {
							return (Class<?>) upperBounds[0];
						}
					}
				}
			}
		}

		return Object.class;
	}
}
