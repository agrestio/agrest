package com.nhl.link.rest.unit.pojo;

import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.PropertyUtils;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseLinkRestService;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class PojoLinkRestService extends BaseLinkRestService {

	private PojoDB db;
	private IConfigMerger configMerger;
	private IMetadataService metadataService;

	public PojoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IConfigMerger configMerger, @Inject IMetadataService metadataService) {
		super(requestParser, encoderService);
		this.db = JerseyTestOnPojo.pojoDB;
		this.configMerger = configMerger;
		this.metadataService = metadataService;
	}

	@Override
	public <T> SelectBuilder<T> forSelect(Class<T> root) {
		return new PojoSelectBuilder<>(root, encoderService, requestParser, configMerger, db.bucketForType(root),
				metadataService);
	}

	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		throw new UnsupportedOperationException("Can't select with Cayenne query");
	}

	@Override
	public <T> SelectBuilder<T> forSelectRelated(Class<?> root, Object rootId, Property<T> relationship) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SelectBuilder<?> forSelectRelated(Class<?> root, Object rootId, String relationship) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected void doDelete(Class<?> root, Object id) {
		db.bucketForType(root).remove(id);
	}

	@Override
	public SimpleResponse relateNew(Class<?> sourceType, Object sourceId, String relationship, String targetData) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SimpleResponse relate(Class<?> sourceType, Object sourceId, String relationship, Object targetId) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected <T> T doInsert(UpdateResponse<T> response) {

		T object;
		try {
			object = response.getType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error creating entity", e);
		}

		mergeChanges(response, object);
		db.bucketForType(response.getType()).put(getId(response, object), object);
		return object;
	}

	@Override
	protected <T> T doUpdate(UpdateResponse<T> response) {
		T object = db.bucketForType(response.getType()).get(response.getId());

		if (object == null) {
			throw new LinkRestException(Status.NOT_FOUND, "Object  with ID '" + response.getId() + "' is not found");
		}

		mergeChanges(response, object);

		return object;
	}

	private <T> Object getId(UpdateResponse<T> response, T pojo) {
		String pkProperty = response.getEntity().getCayenneEntity().getPrimaryKeyNames().iterator().next();
		return PropertyUtils.getProperty(pojo, pkProperty);
	}

	private <T> void mergeChanges(UpdateResponse<T> response, T object) {

		// attributes
		for (Entry<String, Object> e : response.getValues().entrySet()) {
			PropertyUtils.setProperty(object, e.getKey(), e.getValue());
		}
	}

}
