package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.BaseDeleteBuilder;

/**
 * @since 1.4
 */
class CayenneDeleteBuilder<T> extends BaseDeleteBuilder<T> {

	private ICayennePersister persister;
	private ObjectMapper mapper;

	public CayenneDeleteBuilder(Class<T> type, ICayennePersister persister) {
		super(type);
		this.persister = persister;

		// TODO: should setting the mapper be a part of the builder API
		this.mapper = ByIdObjectMapper.mapper();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SimpleResponse delete() {

		ObjectContext context = persister.newContext();

		// dirty ... we have no concept of DeleteResponse, and we need to
		// pass context to the ObjectMapper, so creating a throwaway response
		// and EntityUpdate .. TODO: somehow need to adapt ObjectMapper to
		// delete responses

		ResponseObjectMapper<T> responseMapper = mapper.forResponse(createResponse(context));
		EntityUpdate u = new EntityUpdate();
		u.setId(id);
		T object = responseMapper.find(u);

		if (object == null) {
			ObjEntity entity = context.getEntityResolver().getObjEntity(type);
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		context.deleteObjects(object);
		context.commitChanges();

		return new SimpleResponse(true);
	}

	private CayenneUpdateResponse<T> createResponse(ObjectContext context) {

		ObjEntity entity = context.getEntityResolver().getObjEntity(type);
		Entity<T> clientEntity = new Entity<T>(type, entity);

		CayenneUpdateResponse<T> response = new CayenneUpdateResponse<>(type, context);
		response.parent(parent).withClientEntity(clientEntity);
		return response;
	}

}
