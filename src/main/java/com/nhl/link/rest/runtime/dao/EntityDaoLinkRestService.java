package com.nhl.link.rest.runtime.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.BaseLinkRestService;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * An {@link ILinkRestService} that can work with per-entity pluggable backends.
 * LinkRest automatically includes Cayenne backend, which is configured for all
 * known Cayenne entities. Other backends, e.g. for LDAP objects, etc. are
 * contributed via a collection of {@link IEntityDaoFactory} objects.
 */
public class EntityDaoLinkRestService extends BaseLinkRestService {

	private ConcurrentMap<String, EntityDao<?>> entityDaos;
	private IMetadataService metadataService;
	private IEntityDaoFactory daoFactory;

	public EntityDaoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IMetadataService metadataService, @Inject IConstraintsHandler configMerger,
			@Inject IEntityDaoFactory daoFactory) {
		super(requestParser, encoderService);

		this.metadataService = metadataService;
		this.entityDaos = new ConcurrentHashMap<>();
		this.daoFactory = daoFactory;
	}

	private <T> EntityDao<T> daoForType(Class<T> type) {
		return dao(metadataService.getLrEntity(type));
	}

	private <T> EntityDao<T> daoForQuery(SelectQuery<T> query) {
		return dao(metadataService.getLrEntity(query));
	}

	@SuppressWarnings("unchecked")
	private <T> EntityDao<T> dao(LrEntity<T> entity) {

		EntityDao<T> dao = (EntityDao<T>) entityDaos.get(entity.getName());

		if (dao == null) {
			EntityDao<T> newDao = daoFactory.dao(entity);
			EntityDao<T> oldDao = (EntityDao<T>) entityDaos.putIfAbsent(entity.getName(), newDao);
			dao = oldDao != null ? oldDao : newDao;
		}

		return (EntityDao<T>) dao;
	}

	@Override
	public <T> SelectBuilder<T> select(Class<T> root) {
		return daoForType(root).forSelect();
	}

	@Override
	public <T> SelectBuilder<T> select(SelectQuery<T> query) {
		return daoForQuery(query).forSelect(query);
	}

	/**
	 * @since 1.4
	 */
	@Override
	public <T> DeleteBuilder<T> delete(Class<T> root) {
		return daoForType(root).delete();
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship) {
		daoForType(root).unrelate(sourceId, relationship);
		return new SimpleResponse(true);
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId) {
		daoForType(root).unrelate(sourceId, relationship, targetId);
		return new SimpleResponse(true);
	}

	@Override
	public <T> UpdateBuilder<T> create(Class<T> type) {
		return daoForType(type).create();
	}

	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		return daoForType(type).createOrUpdate();
	}

	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		return daoForType(type).idempotentCreateOrUpdate();
	}

	/**
	 * @since 1.7
	 */
	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		return daoForType(type).idempotentFullSync();
	}

	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		return daoForType(type).update();
	}
}
