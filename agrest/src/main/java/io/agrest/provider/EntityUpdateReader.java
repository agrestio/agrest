package io.agrest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.protocol.IEntityUpdateParser;

/**
 * A provider of {@link MessageBodyReader} for {@link EntityUpdate} parameters.
 * 
 * @since 1.20
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EntityUpdateReader implements MessageBodyReader<EntityUpdate<?>> {

	private EntityUpdateReaderProcessor reader;

	public EntityUpdateReader(@Context Configuration config) {
		this.reader = new EntityUpdateReaderProcessor(AgRuntime.service(IEntityUpdateParser.class, config),
				AgRuntime.service(IMetadataService.class, config));
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EntityUpdate.class.equals(type) && MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
	}

	@Override
	public EntityUpdate<?> readFrom(Class<EntityUpdate<?>> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException, WebApplicationException {

		Collection<EntityUpdate<Object>> updates = reader.read(genericType, entityStream);

		if (updates.isEmpty()) {
			throw new AgException(Status.BAD_REQUEST, "No update");
		}

		if (updates.size() > 1) {
			throw new AgException(Status.BAD_REQUEST, "Expected single update. Found: " + updates.size());
		}

		return updates.iterator().next();
	}

}
