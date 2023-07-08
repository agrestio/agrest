package io.agrest.jaxrs2.provider;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.runtime.jackson.IJacksonService;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class SimpleResponseWriter implements MessageBodyWriter<SimpleResponse> {

    private IJacksonService jacksonService;

    @Context
    private Configuration config;

    @Override
    public long getSize(SimpleResponse t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return SimpleResponse.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(
            SimpleResponse t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        switch (t.getStatus()) {
            case 304:
                return;
            default:
                getJacksonService().outputJson(out -> writeData(t, out), entityStream);
        }
    }

    private IJacksonService getJacksonService() {
        if (jacksonService == null) {
            jacksonService = AgJaxrs.runtime(config).service(IJacksonService.class);
        }

        return jacksonService;
    }

    protected void writeData(SimpleResponse t, JsonGenerator out) throws IOException {
        out.writeStartObject();

        if (t.getMessage() != null) {
            out.writeStringField("message", t.getMessage());
        }

        out.writeEndObject();
    }
}
