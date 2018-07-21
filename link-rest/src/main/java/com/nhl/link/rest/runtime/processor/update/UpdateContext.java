package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.CompoundObjectId;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SimpleObjectId;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.BaseProcessingContext;
import com.nhl.link.rest.LrRequest;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for various updating
 * requests.
 * 
 * @since 1.16
 */
public class UpdateContext<T> extends BaseProcessingContext<T> {

	private ResourceEntity<T> entity;
	private UriInfo uriInfo;
	private LrObjectId id;
	private EntityParent<?> parent;
	private Constraint<T> readConstraints;
	private Constraint<T> writeConstraints;
	private boolean includingDataInResponse;
	private ObjectMapperFactory mapper;
	private String entityData;
	private boolean idUpdatesDisallowed;
	private Collection<EntityUpdate<T>> updates;
	private Encoder encoder;
	private List objects;
	private LrRequest rawRequest;

	public UpdateContext(Class<T> type) {
		super(type);
	}

	/**
	 * Returns a newly created DataResponse object reflecting the context state.
	 * 
	 * @since 1.24
	 * @return a newly created DataResponse object reflecting the context state.
	 */
	public DataResponse<T> createDataResponse() {
		List<T> objects = this.objects != null ? this.objects : Collections.<T> emptyList();
		DataResponse<T> response = DataResponse.forType(getType());
		response.setObjects(objects);
		response.setEncoder(encoder);
		response.setStatus(getStatus());
		return response;
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

	/**
	 * @since 2.13
	 */
	public Map<String, List<String>> getProtocolParameters() {
		return uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
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
		this.id = new SimpleObjectId(id);
	}

	public void setCompoundId(Map<String, Object> ids) {
		this.id = new CompoundObjectId(ids);
	}

	public EntityParent<?> getParent() {
		return parent;
	}

	public void setParent(EntityParent<?> parent) {
		this.parent = parent;
	}

	public Constraint<T> getReadConstraints() {
		return readConstraints;
	}

	public void setReadConstraints(Constraint<T> readConstraints) {
		this.readConstraints = readConstraints;
	}

	public Constraint<T> getWriteConstraints() {
		return writeConstraints;
	}

	public void setWriteConstraints(Constraint<T> writeConstraints) {
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

	/**
	 * @since 1.24
	 */
	public Encoder getEncoder() {
		return encoder;
	}

	/**
	 * @since 1.24
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @since 1.24
	 */
	public List<T> getObjects() {
		return objects;
	}

	/**
	 * @since 1.24
	 */
	public void setObjects(List<? extends T> objects) {
		this.objects = objects;
	}

	/**
	 * @since 2.13
	 */
	public LrRequest getRawRequest() {
		return rawRequest;
	}

	/**
	 * @since 2.13
	 */
	public void setRawRequest(LrRequest rawRequest) {
		this.rawRequest = rawRequest;
	}
}
