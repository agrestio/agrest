package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseUpdateBuilder;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

class CayenneUpdateBuilder<T> extends BaseUpdateBuilder<T> {

	private ICayennePersister persister;

	public CayenneUpdateBuilder(Class<T> type, UpdateOperation op, ICayennePersister persister,
			IEncoderService encoderService, IRequestParser requestParser, IMetadataService metadataService,
			IConstraintsHandler constraintsHandler) {
		super(type, op, encoderService, requestParser, metadataService, constraintsHandler);

		this.persister = persister;
		this.metadataService = metadataService;
	}

	@Override
	protected CayenneUpdateResponse<T> createResponse() {
		CayenneUpdateResponse<T> response = new CayenneUpdateResponse<>(type, persister.newContext());
		response.parent(parent);
		return response;
	}

	@Override
	protected List<T> create(final UpdateResponse<T> response) {
		return process(response, CreateObjectLocator.instance());
	}

	@Override
	protected List<T> update(UpdateResponse<T> response) {
		return process(response, UpdateObjectLocator.instance());
	}

	@Override
	protected List<T> createOrUpdate(final UpdateResponse<T> response) {
		return process(response, CreateOrUpdateObjectLocator.instance());
	}

	@Override
	protected List<T> idempotentCreateOrUpdate(final UpdateResponse<T> response) {

		return process(response, IdempotentCreateOrUpdateObjectLocator.instance());
	}

	private void mergeChanges(ObjEntity entity, EntityUpdate entityUpdate, DataObject object) {

		// attributes
		for (Entry<String, Object> e : entityUpdate.getValues().entrySet()) {
			object.writeProperty(e.getKey(), e.getValue());
		}

		// to-one relationships
		if (!entityUpdate.getRelatedIds().isEmpty()) {
			ObjectContext context = object.getObjectContext();
			for (Entry<String, Object> e : entityUpdate.getRelatedIds().entrySet()) {
				if (e.getValue() == null) {
					object.setToOneTarget(e.getKey(), null, true);
					continue;
				}
				ObjRelationship relationship = (ObjRelationship) entity.getRelationship(e.getKey());
				ClassDescriptor relatedDescriptor = context.getEntityResolver().getClassDescriptor(
						relationship.getTargetEntityName());
				DataObject related = (DataObject) Cayenne.objectForPK(context, relatedDescriptor.getObjectClass(),
						e.getValue());
				if (related == null) {
					throw new LinkRestException(Status.NOT_FOUND, "Related object '"
							+ relationship.getTargetEntityName() + "' with ID '" + e.getValue() + "' is not found");
				}
				object.setToOneTarget(e.getKey(), related, true);
			}
		}
	}

	protected ResponseObjectMapper<T> createObjectMapper(UpdateResponse<T> response) {
		ObjectMapper mapper = this.mapper != null ? this.mapper : ByIdObjectMapper.mapper();
		return mapper.forResponse(response);
	}

	protected List<T> process(UpdateResponse<T> response, ObjectLocator locator) {

		int len = response.getUpdates().size();
		if (len == 0) {
			return Collections.emptyList();
		}

		ObjEntity entity = response.getEntity().getCayenneEntity();

		ResponseObjectMapper<T> mapper = createObjectMapper(response);
		ObjectRelator<T> relator = createRelator(mapper);

		Map<EntityUpdate, T> objects = locator.locate(response, mapper);

		List<T> created = new ArrayList<>(len);

		for (EntityUpdate u : response.getUpdates()) {
			T o = objects.get(u);

			if (o == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unmapped object: " + u.getId());
			}

			mergeChanges(entity, u, (DataObject) o);
			relator.relate(o);
			created.add(o);
		}

		((CayenneUpdateResponse<T>) response).getUpdateContext().commitChanges();
		return created;
	}

	protected ObjectRelator<T> createRelator(ResponseObjectMapper<T> mapper) {

		if (parent == null) {
			return new ObjectRelator<T>() {

				@Override
				public void relate(T object) {
					// do nothing..
				}
			};
		}

		final DataObject parentObject = (DataObject) mapper.findParent();
		if (parentObject == null) {
			ObjEntity entity = metadataService.getObjEntity(parent.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + entity.getName() + "'");
		}

		// TODO: check that relationship target is the same as <T> ??
		if (metadataService.getObjRelationship(parent).isToMany()) {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parentObject.addToManyTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		} else {
			return new ObjectRelator<T>() {
				@Override
				public void relate(T object) {
					parentObject.setToOneTarget(parent.getRelationship(), (DataObject) object, true);
				}
			};
		}
	}

	interface ObjectRelator<T> {
		void relate(T object);
	}
}
