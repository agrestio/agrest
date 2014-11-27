package com.nhl.link.rest.it.fixture.pojo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.runtime.BaseDeleteBuilder;
import com.nhl.link.rest.runtime.BaseLinkRestService;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class PojoLinkRestService extends BaseLinkRestService {

	private PojoDB db;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;

	public PojoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IConstraintsHandler configMerger, @Inject IMetadataService metadataService) {
		super(requestParser, encoderService);
		this.db = JerseyTestOnPojo.pojoDB;
		this.constraintsHandler = configMerger;
		this.metadataService = metadataService;
	}

	@Override
	public <T> SelectBuilder<T> forSelect(Class<T> root) {
		return new PojoSelectBuilder<>(root, encoderService, requestParser, constraintsHandler, db.bucketForType(root));
	}

	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		throw new UnsupportedOperationException("Can't select with Cayenne query");
	}

	@Override
	public <T> DeleteBuilder<T> delete(Class<T> root) {
		return new BaseDeleteBuilder<T>(root) {
			@Override
			public SimpleResponse delete() {
				db.bucketForType(type).remove(id);
				return new SimpleResponse(true);
			}
		};
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
	public <T> UpdateBuilder<T> create(Class<T> type) {
		return new PojoUpdateBuilder<>(db.bucketForType(type), type, UpdateOperation.create, encoderService,
				requestParser, metadataService, constraintsHandler);
	}

	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		return new PojoUpdateBuilder<>(db.bucketForType(type), type, UpdateOperation.createOrUpdate, encoderService,
				requestParser, metadataService, constraintsHandler);
	}

	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		return new PojoUpdateBuilder<>(db.bucketForType(type), type, UpdateOperation.idempotentCreateOrUpdate,
				encoderService, requestParser, metadataService, constraintsHandler);
	}

	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		return new PojoUpdateBuilder<>(db.bucketForType(type), type, UpdateOperation.idempotentFullSync,
				encoderService, requestParser, metadataService, constraintsHandler);
	}

	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		return new PojoUpdateBuilder<>(db.bucketForType(type), type, UpdateOperation.update, encoderService,
				requestParser, metadataService, constraintsHandler);
	}

}
