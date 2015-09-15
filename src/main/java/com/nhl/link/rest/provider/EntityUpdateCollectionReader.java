package com.nhl.link.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IUpdateParser;

/**
 * A provider of {@link {@link MessageBodyReader}} for Collections of
 * {@link EntityUpdate} parameters.
 * 
 * @since 1.20
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EntityUpdateCollectionReader<T> implements MessageBodyReader<Collection<EntityUpdate<T>>> {

	private EntityUpdateReaderProcessor reader;

	public EntityUpdateCollectionReader(@Context Configuration config) {
		this.reader = new EntityUpdateReaderProcessor(LinkRestRuntime.service(IUpdateParser.class, config),
				LinkRestRuntime.service(IMetadataService.class, config));
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!Collection.class.isAssignableFrom(type) || !MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
			return false;
		}

		Type collectionParam = unwrapCollectionParameter(genericType);
		if (collectionParam instanceof ParameterizedType
				&& EntityUpdate.class.equals(((ParameterizedType) collectionParam).getRawType())) {
			return true;
		}

		return false;
	}

	@Override
	public Collection<EntityUpdate<T>> readFrom(Class<Collection<EntityUpdate<T>>> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {

		Type entityUpdateType = unwrapCollectionParameter(genericType);
		if (entityUpdateType == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Invalid request entity collection type: " + genericType);
		}

		return reader.read(entityUpdateType, entityStream);
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
}
