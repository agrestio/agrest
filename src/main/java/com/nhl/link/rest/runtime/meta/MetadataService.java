package com.nhl.link.rest.runtime.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

public class MetadataService implements IMetadataService {

	public static final String NON_PERSISTENT_ENTITIES_LIST = "linkrest.meta.nonpersistent.list";

	private EntityResolver entityResolver;
	private LrDataMap dataMap;

	public MetadataService(@Inject(NON_PERSISTENT_ENTITIES_LIST) List<DataMap> nonPersistentEntities,
			@Inject ICayennePersister cayenneService) {

		EntityResolver cayenneResolver = cayenneService.entityResolver();
		if (nonPersistentEntities.isEmpty()) {
			this.entityResolver = cayenneResolver;
		} else {

			// clone Cayenne resolver to avoid polluting Cayenne stack with
			// POJOs

			Collection<DataMap> dataMaps = new ArrayList<>(cayenneResolver.getDataMaps());
			dataMaps.addAll(nonPersistentEntities);
			this.entityResolver = new EntityResolver(dataMaps);
		}
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
	public Class<?> getType(String entity) {
		ClassDescriptor cd = entityResolver.getClassDescriptor(entity);
		if (cd == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid entity: " + entity);
		}

		return cd.getObjectClass();
	}
}
