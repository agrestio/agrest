package io.agrest.jaxrs.provider;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;
import io.agrest.MetadataResponse;
import io.agrest.jaxrs.AgJaxrs;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JsonConvertable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @since 1.18
 * @deprecated since 5.0 as Ag metadata is deprecated in favor of OpenAPI
 */
@Deprecated
public class MetadataResponseWriter implements MessageBodyWriter<MetadataResponse<?>> {

    private IJacksonService jacksonService;

    @Context
    private Configuration configuration;

    @Override
    public long getSize(MetadataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MetadataResponse.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(final MetadataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {

        getJacksonService().outputJson(new JsonConvertable() {
            @Override
            public void generateJSON(JsonGenerator out) throws IOException {
                out.writeStartObject();

                writeData(t, out);

                out.writeEndObject();
            }
        }, entityStream);
    }

    protected void writeData(MetadataResponse<?> t, JsonGenerator out) throws IOException, AgException {
        t.writeData(out);
    }

    private IJacksonService getJacksonService() {
        if (jacksonService == null) {
            jacksonService = AgJaxrs.runtime(configuration).service(IJacksonService.class);
        }

        return jacksonService;
    }

}
