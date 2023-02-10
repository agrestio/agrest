package io.agrest.jaxrs3.provider;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * A provider of {@link MessageBodyReader} for {@link EntityUpdate} parameters.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EntityUpdateReader implements MessageBodyReader<EntityUpdate<?>> {

    private final EntityUpdateReaderProcessor reader;

    public EntityUpdateReader(@Context Configuration config) {
        this.reader = new EntityUpdateReaderProcessor(
                AgJaxrs.runtime(config).service(IEntityUpdateParser.class),
                AgJaxrs.runtime(config).service(AgSchema.class));
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return EntityUpdate.class.equals(type) && MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
    }

    @Override
    public EntityUpdate<?> readFrom(
            Class<EntityUpdate<?>> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws WebApplicationException {

        Collection<EntityUpdate<Object>> updates = reader.read(genericType, entityStream);

        if (updates.isEmpty()) {
            // null is valid here
            return null;
        }

        if (updates.size() > 1) {
            throw AgException.badRequest("Expected single update. Found: %s", updates.size());
        }

        return updates.iterator().next();
    }

}
