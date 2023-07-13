package io.agrest.jaxrs2.provider;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.protocol.IUpdateRequestParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
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
                AgJaxrs.runtime(config).service(IUpdateRequestParser.class),
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
