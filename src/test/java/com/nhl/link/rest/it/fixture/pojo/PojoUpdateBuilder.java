package com.nhl.link.rest.it.fixture.pojo;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseUpdateBuilder;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import org.apache.cayenne.reflect.PropertyUtils;

import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.Map.Entry;

public class PojoUpdateBuilder<T> extends BaseUpdateBuilder<T> {

	private Map<Object, T> typeBucket;

	public PojoUpdateBuilder(Map<Object, T> typeBucket, Class<T> type, UpdateOperation op,
			IEncoderService encoderService, IRequestParser requestParser, IMetadataService metadataService,
			IConstraintsHandler constraintsHandler) {
		super(type, op, encoderService, requestParser, metadataService, constraintsHandler);
		this.typeBucket = typeBucket;
	}

	protected UpdateResponse<T> createResponse() {
		return new UpdateResponse<>(type).parent(parent);
	}

	@Override
	protected void fetchObjects(UpdateResponse<T> responseBuilder) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected void create(UpdateResponse<T> response) {

		T object;
		try {
			object = response.getType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error creating entity", e);
		}

		mergeChanges(response, object);
		typeBucket.put(getId(response, object), object);
	}

	@Override
	protected void update(UpdateResponse<T> response) {
		T object = typeBucket.get(response.getFirst().getId());

		if (object == null) {
			throw new LinkRestException(Status.NOT_FOUND, "Object  with ID '" + response.getFirst().getId()
					+ "' is not found");
		}

		mergeChanges(response, object);
	}

	@Override
	protected void createOrUpdate(UpdateResponse<T> response) {
		throw new UnsupportedOperationException("TODO");

	}

	@Override
	protected void idempotentCreateOrUpdate(UpdateResponse<T> response) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected void idempotentFullSync(UpdateResponse<T> response) {
		throw new UnsupportedOperationException("TODO");
	}

	private Object getId(UpdateResponse<T> response, T pojo) {
		String pkProperty = response.getEntity().getLrEntity().getSingleId().getName();
		return PropertyUtils.getProperty(pojo, pkProperty);
	}

	private void mergeChanges(UpdateResponse<T> response, T object) {

		// attributes
		for (Entry<String, Object> e : response.getFirst().getValues().entrySet()) {
			PropertyUtils.setProperty(object, e.getKey(), e.getValue());
		}

		response.getFirst().setMergedTo(object);
	}
}
