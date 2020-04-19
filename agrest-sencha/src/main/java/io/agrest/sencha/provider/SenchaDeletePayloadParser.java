package io.agrest.sencha.provider;

import io.agrest.AgException;
import io.agrest.CompoundObjectId;
import io.agrest.EntityDelete;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.base.jsonvalueconverter.IJsonValueConverterFactory;
import io.agrest.runtime.protocol.EntityUpdateJsonTraverser;
import io.agrest.runtime.protocol.EntityUpdateJsonVisitor;
import io.agrest.runtime.semantics.IRelationshipMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class SenchaDeletePayloadParser implements MessageBodyReader<Collection<EntityDelete<?>>> {

    private IJacksonService jacksonService;
	private IMetadataService metadataService;
	private EntityUpdateJsonTraverser entityUpdateJsonTraverser;

    public SenchaDeletePayloadParser(@Context Configuration config) {
        this.jacksonService = AgRuntime.service(IJacksonService.class, config);
		this.metadataService = AgRuntime.service(IMetadataService.class, config);
		this.entityUpdateJsonTraverser = new EntityUpdateJsonTraverser(AgRuntime.service(IRelationshipMapper.class, config),
				AgRuntime.service(IJsonValueConverterFactory.class, config));
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!Collection.class.equals(type) || !MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
			return false;
		}

		Type collectionParam = unwrapCollectionParameter(genericType);
		return collectionParam instanceof ParameterizedType
				&& EntityDelete.class.equals(((ParameterizedType) collectionParam).getRawType());
	}

    @Override
    public Collection<EntityDelete<?>> readFrom(Class<Collection<EntityDelete<?>>> type, Type genericType, Annotation[] annotations,
                                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                           InputStream entityStream) throws IOException, WebApplicationException {

		Type entityType = unwrapCollectionParameter(genericType);
		if (entityType == null) {
			throw new AgException(Status.INTERNAL_SERVER_ERROR,
					"Invalid request entity collection type: " + genericType);
		}

		AgEntity<?> entity = metadataService.getAgEntityByType(entityType);

		DeleteVisitor visitor = new DeleteVisitor(entity);
        entityUpdateJsonTraverser.traverse(entity, jacksonService.parseJson(entityStream), visitor);
		return visitor.getDeleted();
    }

	Type unwrapCollectionParameter(Type genericCollectionType) {

		if (!(genericCollectionType instanceof ParameterizedType)) {
			return null;
		}

		Type[] typeArgs = ((ParameterizedType) genericCollectionType).getActualTypeArguments();
		if (typeArgs.length != 1) {
			return null;
		}

		return typeArgs[0];
	}

	private static class DeleteVisitor implements EntityUpdateJsonVisitor {

		private AgEntity<?> entity;
		private Collection<EntityDelete<?>> deleted;

		private Map<String, Object> deletedId;

		protected DeleteVisitor(AgEntity<?> entity) {
			this.entity = entity;
			this.deleted = new ArrayList<>();
		}

		@Override
		public void beginObject() {
			deletedId = new HashMap<>();
		}

		@Override
		public void visitId(String name, Object value) {
			deletedId.put(name, value);
		}

		@Override
		public void visitAttribute(String name, Object value) {
			deletedId.put(name, value);
		}

		@Override
		public void visitRelationship(String name, Object relatedId) {
			deletedId.put(name, relatedId);
		}

		@Override
		public void endObject() {
			if (deletedId.isEmpty()) {
				throw new AgException(Status.BAD_REQUEST, "Object id is empty");
			} else if (deletedId.size() == 1) {
				deleted.add(new EntityDelete<>(entity, new SimpleObjectId(deletedId.values().iterator().next())));
			} else {
				deleted.add(new EntityDelete<>(entity, new CompoundObjectId(deletedId)));
			}
			deletedId = null;
		}

		public Collection<EntityDelete<?>> getDeleted() {
			return deleted;
		}
	}
}
