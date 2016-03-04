package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrOperation;
import com.nhl.link.rest.meta.LrResource;

import java.io.IOException;
import java.util.Collection;

/**
 * @since 1.18
 */
public class ResourceEncoder<T> extends AbstractEncoder {

    private LrEntity<T> entity;
    private String applicationBase;
    private Encoder entityEncoder;

    public ResourceEncoder(LrEntity<T> entity, String applicationBase, Encoder entityEncoder) {
        this.entity = entity;
        this.applicationBase = applicationBase == null? "" : applicationBase;
        this.entityEncoder = entityEncoder;
    }

    @SuppressWarnings("unchecked")
	@Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        writeEntity(entity, out);

        writeResources((Collection<LrResource<?>>) object, out);

        return true;
    }

    private void writeResources(Collection<LrResource<?>> resources, JsonGenerator out) throws IOException {
        out.writeArrayFieldStart("links");

        for (LrResource<?> resource : resources) {
            writeResource(resource, out);
        }

        out.writeEndArray();
    }

    private void writeResource(LrResource<?> resource, JsonGenerator out) throws IOException{

        out.writeStartObject();

        out.writeStringField("href", applicationBase + resource.getPath());

        out.writeStringField("type", resource.getType().name().toLowerCase());

        writeOperations(resource.getOperations(), out);

        out.writeEndObject();

    }

    private void writeOperations(Collection<LrOperation> operations, JsonGenerator out) throws IOException {
        out.writeArrayFieldStart("operations");
        for (LrOperation operation : operations) {
            out.writeStartObject();
            out.writeStringField("method", operation.getMethod().name());
            out.writeEndObject();
        }
        out.writeEndArray();
    }

    private void writeEntity(LrEntity<T> entity, JsonGenerator out) throws IOException {
        entityEncoder.encode("entity", entity, out);
    }
}
