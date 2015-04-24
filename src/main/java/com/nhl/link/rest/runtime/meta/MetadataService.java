package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.meta.parser.IResourceParser;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;

import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetadataService implements IMetadataService {

	/**
	 * A DI key that allows loading arbitrary user-provided entities into
	 * LinkRest metadata store. Usually used to map POJO entities.
	 *
	 * @since 1.12
	 */
	public static final String EXTRA_ENTITIES_LIST = "linkrest.meta.entity.extras.list";

	/**
	 * A DI key that allows to expand the model of persistent entities coming
	 * form Cayenne.
	 *
	 * @since 1.12
	 */
	public static final String ENTITY_OVERLAY_MAP = "linkrest.meta.entity.overlay.map";

	private EntityResolver entityResolver;
	private LrDataMap dataMap;
	private IResourceParser resourceParser;
	private ConcurrentMap<Class, Collection<LrResource>> classResources;

	public MetadataService(@Inject LrDataMap dataMap, @Inject ICayennePersister cayenneService,
						   @Inject IResourceParser resourceParser) {

		this.entityResolver = cayenneService.entityResolver();
		this.dataMap = dataMap;
		this.resourceParser = resourceParser;
		this.classResources = new ConcurrentHashMap<>();
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
	public Collection<LrResource> getLrResources(Class<?> resourceClass) {

		Collection<LrResource> resources = classResources.get(resourceClass);
		if (resources == null) {
			Collection<LrResource> newResources = resourceParser.parse(resourceClass);

			Collection<LrResource> existingResources = classResources.putIfAbsent(resourceClass, newResources);
			resources = existingResources == null? newResources : existingResources;
		}

		return resources;
	}

}
