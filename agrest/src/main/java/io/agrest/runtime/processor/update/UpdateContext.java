package io.agrest.runtime.processor.update;

import io.agrest.AgRESTException;
import io.agrest.AgRequest;
import io.agrest.CompoundObjectId;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.AgObjectId;
import io.agrest.ObjectMapperFactory;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.processor.BaseProcessingContext;

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
	private AgObjectId id;
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
	private AgRequest rawRequest;
	private AgRequest request;

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
			throw new AgRESTException(Status.INTERNAL_SERVER_ERROR,
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

	public AgObjectId getId() {
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
	 * Returns AgRequest that contains query parameters.
	 * This parameters are used on the CreateEntityStage to create an entity.
	 *
	 * @since 2.13
	 */
	public AgRequest getRawRequest() {
		return rawRequest;
	}

	/**
	 * Saves AgRequest that contains query parameters.
	 *
	 * This AgRequest object is build from two sources.
	 * 1. Parse UriInfo and create query parameters objects.
	 * 2. If some of query parameters are passed explicitly they will be used instead of parsing from UriInfo.
	 * These explicit query parameters are saved in rawRequest object during ParseRequestStage.
	 *
	 * @since 2.13
	 */
	public void setRawRequest(AgRequest rawRequest) {
		this.rawRequest = rawRequest;
	}

	/**
	 * Returns AgRequest object that contains query parameters explicitly passed through API method call
	 *
	 * @since 2.13
	 */
	public AgRequest getRequest() {
		return request;
	}

	/**
	 * Saves AgRequest object that contains query parameters explicitly passed through API method call.
	 * These parameters are created during ConvertQueryParamsStage.
	 *
	 * <pre>{@code
	 *
	 * 		public DataResponse<E2> getE2(@Context UriInfo uriInfo, @QueryParam CayenneExp cayenneExp) {
	 * 			// Explicit query parameter
	 * 			AgRequest agRequest = AgRequest.builder().cayenneExp(cayenneExp).build();
	 *
	 * 			return AgREST.service(config).select(E2.class)
	 * 							.uri(uriInfo)
	 * 							.request(agRequest) // overrides parameters from uriInfo
	 * 							.get();
	 * 		}
	 *
	 * }</pre>
	 *
	 * @since 2.13
	 */
	public void setRequest(AgRequest request) {
		this.request = request;
	}
}
