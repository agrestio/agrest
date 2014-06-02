package com.nhl.link.rest.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.EntityConfig;
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
	private EntityResolver entityResolver;

	public EntityDaoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IMetadataService metadataService, @Inject ICayennePersister cayenneService,
			@Inject IConfigMerger configMerger) {
		super(requestParser, encoderService);

		this.metadataService = metadataService;
		this.entityDaos = new HashMap<>();

		this.entityResolver = cayenneService.entityResolver();
		for (ObjEntity e : entityResolver.getObjEntities()) {

			if (!entityDaos.containsKey(e.getName())) {

				ClassDescriptor cd = entityResolver.getClassDescriptor(e.getName());
				EntityDao<?> dao = new CayenneDao<>(cd.getObjectClass(), requestParser, encoderService, cayenneService,
						configMerger);
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

	@Override
	public DataResponseConfig newConfig(Class<?> root) {

		ObjEntity entity = entityResolver.getObjEntity(root);
		if (entity == null) {
			throw new IllegalArgumentException("Unsupported entity type: " + root.getName());
		}

		// TODO: here we might start with a clone of default config, either for
		// the entire project or the entity.

		DataResponseConfig config = new DataResponseConfig(entity);

		// apply defaults:
		EntityConfig entityConfig = config.getEntity().includeId();
		for (ObjAttribute a : entity.getAttributes()) {
			entityConfig.attribute(a.getName());
		}

		return config;
	}

	@SuppressWarnings("unchecked")
	private <T> EntityDao<T> dao(String entityName) {
		EntityDao<?> dao = entityDaos.get(entityName);

		if (dao == null) {
			throw new IllegalArgumentException("Unsupported entity: " + entityName);
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
