package com.nhl.link.rest.sencha.provider;

import com.nhl.link.rest.CompoundObjectId;
import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SimpleObjectId;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.protocol.EntityUpdateJsonTraverser;
import com.nhl.link.rest.runtime.protocol.EntityUpdateJsonVisitor;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

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
        this.jacksonService = LinkRestRuntime.service(IJacksonService.class, config);
		this.metadataService = LinkRestRuntime.service(IMetadataService.class, config);
		this.entityUpdateJsonTraverser = new EntityUpdateJsonTraverser(LinkRestRuntime.service(IRelationshipMapper.class, config),
				LinkRestRuntime.service(IJsonValueConverterFactory.class, config));
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
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Invalid request entity collection type: " + genericType);
		}

		LrEntity<?> entity = metadataService.getEntityByType(entityType);

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

		private LrEntity<?> entity;
		private Collection<EntityDelete<?>> deleted;

		private Map<String, Object> deletedId;

		protected DeleteVisitor(LrEntity<?> entity) {
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
		public void visitId(Map<String, Object> value) {
			deletedId.putAll(value);
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
				throw new LinkRestException(Status.BAD_REQUEST, "Object id is empty");
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
