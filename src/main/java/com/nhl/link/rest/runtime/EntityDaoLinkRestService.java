package com.nhl.link.rest.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.cayenne.CayenneDao;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * An {@link ILinkRestService} that can work with per-entity pluggable backends.
 * Cayenne backend is automatically configured for all known Cayenne entities.
 * Other backends, e.g. for LDAP objects, etc. are contributed in an
 * {@link EntityDao} collection.
 */
public class EntityDaoLinkRestService extends BaseLinkRestService {

	private Map<String, EntityDao<?>> entityDaos;
	private IMetadataService metadataService;

	public EntityDaoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IMetadataService metadataService, @Inject ICayennePersister cayenneService,
			@Inject IConfigMerger configMerger) {
		super(requestParser, encoderService);

		this.metadataService = metadataService;
		this.entityDaos = new HashMap<>();

		EntityResolver resolver = cayenneService.entityResolver();
		for (ObjEntity e : resolver.getObjEntities()) {

			if (!entityDaos.containsKey(e.getName())) {

				ClassDescriptor cd = resolver.getClassDescriptor(e.getName());
				EntityDao<?> dao = new CayenneDao<>(cd.getObjectClass(), requestParser, encoderService, cayenneService,
						configMerger, metadataService);
				entityDaos.put(e.getName(), dao);
			}
		}
	}

	private <T> EntityDao<T> daoForType(Class<T> type) {
		return dao(metadataService.getObjEntity(type).getName());
	}

	private <T> EntityDao<T> daoForQuery(SelectQuery<T> query) {
		return dao(metadataService.getObjEntity(query).getName());
	}

	@SuppressWarnings("unchecked")
	private <T> EntityDao<T> dao(String entityName) {
		EntityDao<?> dao = entityDaos.get(entityName);

		if (dao == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Unsupported entity: " + entityName);
		}

		return (EntityDao<T>) dao;
	}

	@Override
	public <T> SelectBuilder<T> forSelect(Class<T> root) {
		return daoForType(root).forSelect();
	}

	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		return daoForQuery(query).forSelect(query);
	}

	@Override
	protected <T> void doDelete(Class<T> root, Object id) {
		daoForType(root).delete(id);
	}

	@Override
	protected <T> T doInsert(UpdateResponse<T> response) {
		return daoForType(response.getType()).insert(response);
	}

	@Override
	protected <T> T doUpdate(UpdateResponse<T> response) {
		return daoForType(response.getEntity().getType()).update(response);
	}

}
