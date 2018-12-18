package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgOperation;
import io.agrest.meta.AgResource;

import java.io.IOException;
import java.util.Collection;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @since 1.18
 */
public class ResourceEncoder<T> extends AbstractEncoder {

    private ResourceEntity<T, ?> entity;
    private String applicationBase;
    private Encoder entityEncoder;

    public ResourceEncoder(ResourceEntity<T, ?> entity, String applicationBase, Encoder entityEncoder) {
        this.entity = entity;
        this.applicationBase = applicationBase == null ? "" : applicationBase;
        this.entityEncoder = entityEncoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        writeEntity(entity, out);

        writeResources((Collection<AgResource<?>>) object, out);

        return true;
    }

    private void writeResources(Collection<AgResource<?>> resources, JsonGenerator out) throws IOException {
        out.writeArrayFieldStart("links");

        for (AgResource<?> resource : resources) {
            writeResource(resource, out);
        }

        out.writeEndArray();
    }

    private void writeResource(AgResource<?> resource, JsonGenerator out) throws IOException {

        out.writeStartObject();

        out.writeStringField("href", applicationBase + resource.getPath());

        out.writeStringField("type", resource.getType().name().toLowerCase());

        writeOperations(resource.getOperations(), out);

        out.writeEndObject();

    }

    private void writeOperations(Collection<AgOperation> operations, JsonGenerator out) throws IOException {
        out.writeArrayFieldStart("operations");

        // sort operations for encoding consistency
        Collection<AgOperation> sorted = operations.size() > 1
                ? operations.stream().sorted(comparing(op -> op.getMethod().name())).collect(toList())
                : operations;

        for (AgOperation operation : sorted) {
            out.writeStartObject();
            out.writeStringField("method", operation.getMethod().name());
            out.writeEndObject();
        }
        out.writeEndArray();
    }

    private void writeOperation(AgOperation operation, JsonGenerator out) throws IOException {
        out.writeStartObject();
        out.writeStringField("method", operation.getMethod().name());
        out.writeEndObject();
    }

    private void writeEntity(ResourceEntity<T, ?> entity, JsonGenerator out) throws IOException {
        entityEncoder.encode("entity", entity, out);
    }
}
