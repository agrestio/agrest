package io.agrest.runtime.meta;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.Types;
import io.agrest.meta.compiler.AgEntityCompiler;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import java.lang.reflect.Type;
import java.util.List;

public class MetadataService implements IMetadataService {

	private AgDataMap dataMap;

	public MetadataService(@Inject List<AgEntityCompiler> entityCompilers) {

		this.dataMap = new LazyAgDataMap(entityCompilers);
	}

	/**
	 * @since 1.12
	 */
	@Override
	public <T> AgEntity<T> getAgEntity(Class<T> type) {
		if (type == null) {
			throw new NullPointerException("Null type");
		}

		AgEntity<T> e = dataMap.getEntity(type);

		if (e == null) {
			throw new AgException(Status.BAD_REQUEST, "Invalid entity: " + type.getName());
		}

		return e;
	}

	@Override
	public <T> AgEntity<T> getEntityByType(Type entityType) {
		@SuppressWarnings("unchecked")
		AgEntity<T> entity = getAgEntity( (Class<T>) Types.getClassForTypeArgument(entityType).orElse(Object.class));
		if (entity == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR,
					"EntityUpdate type '" + entityType.getTypeName() + "' is not an entity");
		}
		return entity;
	}
}
