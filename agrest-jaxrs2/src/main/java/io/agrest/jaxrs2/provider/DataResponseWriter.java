package io.agrest.jaxrs2.provider;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;
import io.agrest.encoder.EncodingPolicy;
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
public class DataResponseWriter implements MessageBodyWriter<DataResponse<?>> {

    @Context
    private Configuration config;

    private IJacksonService jacksonService;
    private EncodingPolicy encodingPolicy;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(
            DataResponse<?> t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
            DataResponse<?> t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
            throws IOException {


        boolean skipNullProperties = getEncodingPolicy().skipNullProperties();
        getJacksonService().outputJson(out -> writeData(t, skipNullProperties, out), entityStream);
    }

    private IJacksonService getJacksonService() {
        if (jacksonService == null) {
            jacksonService = AgJaxrs.runtime(config).service(IJacksonService.class);
        }

        return jacksonService;
    }

    private EncodingPolicy getEncodingPolicy() {
        if (encodingPolicy == null) {
            encodingPolicy = AgJaxrs.runtime(config).service(EncodingPolicy.class);
        }

        return encodingPolicy;
    }

    protected void writeData(DataResponse<?> t, boolean skipNullProperties, JsonGenerator out) throws IOException {
        t.getEncoder().encode(null, t, skipNullProperties, out);
    }
}
