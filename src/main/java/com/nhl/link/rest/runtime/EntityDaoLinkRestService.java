package com.nhl.link.rest.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConfigBuilder;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> SelectBuilder<T> forSelectRelated(Class<?> root, Object rootId, Property<T> relationship) {
		return (SelectBuilder<T>) forSelectRelated(root, rootId, relationship.getName());
	}

	@Override
	public SelectBuilder<?> forSelectRelated(Class<?> root, Object rootId, String relationship) {
		ObjRelationship objRelationship = metadataService.getObjRelationship(root, relationship);

		// navigate through DbRelationships ... there may be no reverse ObjRel..
		// Reverse DB should always be there
		Expression qualifier = ExpressionFactory.matchDbExp(objRelationship.getReverseDbRelationshipPath(), rootId);
		EntityConfigBuilder entityConfig = EntityConfigBuilder.config().and(qualifier);
		return dao(objRelationship.getTargetEntityName()).forSelect().withEntity(entityConfig);
	}

	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		return daoForQuery(query).forSelect(query);
	}

	@Override
	protected void doDelete(Class<?> root, Object id) {
		daoForType(root).delete(id);
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
	protected <T> T doInsert(UpdateResponse<T> response) {
		return daoForType(response.getType()).insert(response);
	}

	@Override
	protected <T> T doUpdate(UpdateResponse<T> response) {
		return daoForType(response.getEntity().getType()).update(response);
	}

	@Override
	public DataResponse<?> insertRelated(Class<?> sourceType, Object sourceId, String relationship, String targetData) {

		ObjRelationship objRelationship = metadataService.getObjRelationship(sourceType, relationship);
		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());

		@SuppressWarnings("unchecked")
		UpdateResponse<Object> targetInsert = (UpdateResponse<Object>) requestParser.parseInsert(new UpdateResponse<>(
				targetType), targetData);

		// TODO: change...
		Object target = daoForType(sourceType).relate(sourceId, relationship, targetInsert);
		return targetInsert.withObject(target).withEncoder(encoderService.makeEncoder(targetInsert));
	}

	@Override
	public DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship,
			Object targetId, String targetData) {

		ObjRelationship objRelationship = metadataService.getObjRelationship(sourceType, relationship);
		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());

		@SuppressWarnings("unchecked")
		UpdateResponse<Object> targetUpdate = (UpdateResponse<Object>) requestParser.parseUpdate(new UpdateResponse<>(
				targetType), targetId, targetData);

		daoForType(sourceType).insertOrUpdateRelated(sourceId, relationship, targetUpdate);

		return targetUpdate.withEncoder(encoderService.makeEncoder(targetUpdate));
	}

	@Override
	public DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship,
			String targetData) {
		ObjRelationship objRelationship = metadataService.getObjRelationship(sourceType, relationship);
		Class<?> targetType = metadataService.getType(objRelationship.getTargetEntityName());

		@SuppressWarnings("unchecked")
		UpdateResponse<Object> targetUpdate = (UpdateResponse<Object>) requestParser.parseUpdate(new UpdateResponse<>(
				targetType), targetData);

		daoForType(sourceType).insertOrUpdateRelated(sourceId, relationship, targetUpdate);

		return targetUpdate.withEncoder(encoderService.makeEncoder(targetUpdate));
	}
}
