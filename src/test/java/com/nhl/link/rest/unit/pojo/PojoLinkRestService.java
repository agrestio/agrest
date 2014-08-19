package com.nhl.link.rest.unit.pojo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.CreateOrUpdateBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.BaseLinkRestService;
import com.nhl.link.rest.runtime.CreateOrUpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class PojoLinkRestService extends BaseLinkRestService {

	private PojoDB db;
	private IConstraintsHandler configMerger;
	private IMetadataService metadataService;

	public PojoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IConstraintsHandler configMerger, @Inject IMetadataService metadataService) {
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
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <T> CreateOrUpdateBuilder<T> create(Class<T> type) {
		return new PojoCreateOrUpdateBuilder<>(db.bucketForType(type), type, CreateOrUpdateOperation.create,
				encoderService, requestParser, metadataService);
	}

	@Override
	public <T> CreateOrUpdateBuilder<T> createOrUpdate(Class<T> type) {
		return new PojoCreateOrUpdateBuilder<>(db.bucketForType(type), type, CreateOrUpdateOperation.createOrUpdate,
				encoderService, requestParser, metadataService);
	}

	@Override
	public <T> CreateOrUpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		return new PojoCreateOrUpdateBuilder<>(db.bucketForType(type), type,
				CreateOrUpdateOperation.idempotentCreateOrUpdate, encoderService, requestParser, metadataService);
	}

	@Override
	public <T> CreateOrUpdateBuilder<T> update(Class<T> type) {
		return new PojoCreateOrUpdateBuilder<>(db.bucketForType(type), type, CreateOrUpdateOperation.update,
				encoderService, requestParser, metadataService);
	}
}
