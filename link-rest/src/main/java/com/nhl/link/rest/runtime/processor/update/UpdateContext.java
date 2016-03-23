package com.nhl.link.rest.runtime.processor.update;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * Maintains state of the request processing chain for various updating
 * requests.
 * 
 * @since 1.16
 */
public class UpdateContext<T> extends BaseProcessingContext<T> {

	private DataResponse<T> response;
	private ResourceEntity<T> entity;
	private UriInfo uriInfo;
	private LrObjectId id;
	private EntityParent<?> parent;
	private ConstraintsBuilder<T> readConstraints;
	private ConstraintsBuilder<T> writeConstraints;
	private boolean includingDataInResponse;
	private ObjectMapperFactory mapper;
	private String entityData;
	private boolean idUpdatesDisallowed;
	private Collection<EntityUpdate<T>> updates;

	public UpdateContext(Class<T> type) {
		super(type);
	}

	public DataResponse<T> getResponse() {
		return response;
	}

	public void setResponse(DataResponse<T> response) {
		this.response = response;
	}

	/**
	 * @since 1.19
	 */
	public boolean hasChanges() {

		for (EntityUpdate<T> u : updates) {
			if (u.hasChanges()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @since 1.19
	 */
	public Collection<EntityUpdate<T>> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<EntityUpdate<T>> updates) {
		this.updates = updates;
	}

	/**
	 * Returns first update object. Throws unless this response contains exactly
	 * one update.
	 * 
	 * @since 1.19
	 */
	public EntityUpdate<T> getFirst() {

		Collection<EntityUpdate<T>> updates = getUpdates();

		if (updates.size() != 1) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Expected one object in update. Actual: " + updates.size());
		}

		return updates.iterator().next();
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public boolean isById() {
		return id != null;
	}

	public LrObjectId getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = new LrObjectId(id);
	}

	public void setCompoundId(Map<String, Object> ids) {
		this.id = new LrObjectId(ids);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public ConstraintsBuilder<T> getReadConstraints() {
		return readConstraints;
	}

	public void setReadConstraints(ConstraintsBuilder<T> readConstraints) {
		this.readConstraints = readConstraints;
	}

	public ConstraintsBuilder<T> getWriteConstraints() {
		return writeConstraints;
	}

	public void setWriteConstraints(ConstraintsBuilder<T> writeConstraints) {
		this.writeConstraints = writeConstraints;
	}

	public boolean isIncludingDataInResponse() {
		return includingDataInResponse;
	}

	public void setIncludingDataInResponse(boolean includeData) {
		this.includingDataInResponse = includeData;
	}

	public ObjectMapperFactory getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapperFactory mapper) {
		this.mapper = mapper;
	}

	public String getEntityData() {
		return entityData;
	}

	public void setEntityData(String entityData) {
		this.entityData = entityData;
	}

	/**
	 * @since 1.19
	 */
	public boolean isIdUpdatesDisallowed() {
		return idUpdatesDisallowed;
	}

	/**
	 * @since 1.19
	 */
	public void setIdUpdatesDisallowed(boolean idUpdatesDisallowed) {
		this.idUpdatesDisallowed = idUpdatesDisallowed;
	}

	/**
	 * @since 1.20
	 */
	public ResourceEntity<T> getEntity() {
		return entity;
	}

	/**
	 * @since 1.20
	 */
	public void setEntity(ResourceEntity<T> entity) {
		this.entity = entity;
	}
}
