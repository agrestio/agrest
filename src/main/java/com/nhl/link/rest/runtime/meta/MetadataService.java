package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.cayenne.CayenneAwareLrDataMap;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.Select;

import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;

public class MetadataService implements IMetadataService {

	private EntityResolver entityResolver;
	private LrDataMap dataMap;

	public MetadataService(@Inject List<LrEntity> extraEntities,
			@Inject Map<String, LrEntityOverlay> entityOverlays,
			@Inject ICayennePersister cayenneService) {

		this.entityResolver = cayenneService.entityResolver();
		this.dataMap = new CayenneAwareLrDataMap(entityResolver, extraEntities, entityOverlays);
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
}
