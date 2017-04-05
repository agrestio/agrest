package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;

import javax.ws.rs.core.Response.Status;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

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
	@Deprecated
	@Override
	public <T> LrEntity<T> getLrEntity(Select<T> query) {
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) query.getMetaData(entityResolver).getClassDescriptor().getObjectClass();
		return getLrEntity(type);
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
