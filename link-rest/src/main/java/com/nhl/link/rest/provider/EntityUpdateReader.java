package com.nhl.link.rest.provider;

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

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IUpdateParser;

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
		this.reader = new EntityUpdateReaderProcessor(LinkRestRuntime.service(IUpdateParser.class, config),
				LinkRestRuntime.service(IMetadataService.class, config));
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
			throw new LinkRestException(Status.BAD_REQUEST, "No update");
		}

		if (updates.size() > 1) {
			throw new LinkRestException(Status.BAD_REQUEST, "Expected single update. Found: " + updates.size());
		}

		return updates.iterator().next();
	}

}
