package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.Types;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;

import javax.ws.rs.core.Response.Status;
import java.lang.reflect.Type;
import java.util.List;

public class MetadataService implements IMetadataService {

	private EntityResolver entityResolver;
	private LrDataMap dataMap;

	public MetadataService(@Inject List<LrEntityCompiler> entityCompilers, @Inject ICayennePersister cayenneService) {

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

	@Override
	public <T> LrEntity<T> getEntityByType(Type entityType) {
		@SuppressWarnings("unchecked")
		LrEntity<T> entity = getLrEntity( (Class<T>) Types.getClassForTypeArgument(entityType).orElse(Object.class));
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"EntityUpdate type '" + entityType.getTypeName() + "' is not an entity");
		}
		return entity;
	}
}
