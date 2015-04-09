package com.nhl.link.rest.runtime.processor.update;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * Maintains state of the request processing chain for various updating
 * requests.
 * 
 * @since 1.16
 */
public class UpdateContext<T> extends BaseProcessingContext<T> {

	private UpdateResponse<T> response;
	private UriInfo uriInfo;
	private Object id;
	private EntityParent<?> parent;
	private ConstraintsBuilder<T> readConstraints;
	private ConstraintsBuilder<T> writeConstraints;
	private boolean includingDataInResponse;
	private ObjectMapperFactory mapper;
	private String entityData;

	public UpdateContext(Class<T> type) {
		super(type);
	}

	public UpdateResponse<T> getResponse() {
		return response;
	}

	public void setResponse(UpdateResponse<T> response) {
		this.response = response;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
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
}
