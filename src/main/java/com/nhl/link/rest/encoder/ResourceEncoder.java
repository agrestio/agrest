package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrOperation;
import com.nhl.link.rest.meta.LrResource;

import java.io.IOException;
import java.util.Collection;

public class ResourceEncoder extends AbstractEncoder {

    private String applicationBase;

    public ResourceEncoder(String applicationBase) {
        this.applicationBase = applicationBase == null? "" : applicationBase;
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        writeResource((LrResource) object, out);
        return true;
    }

    private void writeResource(LrResource resource, JsonGenerator out) throws IOException{
        out.writeObjectFieldStart("data");

        out.writeStringField("href", applicationBase + "/" + resource.getPath());

        out.writeStringField("type", "");

        writeOperations(resource.getOperations(), out);

        writeEntity(resource.getEntity(), out);

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

    private void writeEntity(LrEntity entity, JsonGenerator out) throws IOException {
        out.writeObjectFieldStart("entity");
        out.writeEndObject();
    }

}
