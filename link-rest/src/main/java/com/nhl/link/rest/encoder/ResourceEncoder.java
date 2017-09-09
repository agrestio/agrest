package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrOperation;
import com.nhl.link.rest.meta.LrResource;

import java.io.IOException;
import java.util.Collection;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @since 1.18
 */
public class ResourceEncoder<T> extends AbstractEncoder {

    private ResourceEntity<T> entity;
    private String applicationBase;
    private Encoder entityEncoder;

    public ResourceEncoder(ResourceEntity<T> entity, String applicationBase, Encoder entityEncoder) {
        this.entity = entity;
        this.applicationBase = applicationBase == null ? "" : applicationBase;
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

    private void writeResource(LrResource<?> resource, JsonGenerator out) throws IOException {

        out.writeStartObject();

        out.writeStringField("href", applicationBase + resource.getPath());

        out.writeStringField("type", resource.getType().name().toLowerCase());

        writeOperations(resource.getOperations(), out);

        out.writeEndObject();

    }

    private void writeOperations(Collection<LrOperation> operations, JsonGenerator out) throws IOException {
        out.writeArrayFieldStart("operations");

        // sort operations for encoding consistency
        Collection<LrOperation> sorted = operations.size() > 1
                ? operations.stream().sorted(comparing(op -> op.getMethod().name())).collect(toList())
                : operations;

        for (LrOperation operation : sorted) {
            out.writeStartObject();
            out.writeStringField("method", operation.getMethod().name());
            out.writeEndObject();
        }
        out.writeEndArray();
    }

    private void writeOperation(LrOperation operation, JsonGenerator out) throws IOException {
        out.writeStartObject();
        out.writeStringField("method", operation.getMethod().name());
        out.writeEndObject();
    }

    private void writeEntity(ResourceEntity<T> entity, JsonGenerator out) throws IOException {
        entityEncoder.encode("entity", entity, out);
    }
}
