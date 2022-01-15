package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @since 2.1
 */
public class DataResponseEncoder implements Encoder {

    private final CollectionEncoder resultEncoder;
    private final Encoder totalEncoder;
    private final String resultProperty;
    private final String totalProperty;

    public DataResponseEncoder(
            String resultProperty,
            CollectionEncoder resultEncoder,
            String totalProperty,
            Encoder totalEncoder) {

        this.totalProperty = totalProperty;
        this.resultEncoder = resultEncoder;
        this.totalEncoder = totalEncoder;
        this.resultProperty = resultProperty;
    }

    @Override
    public void encode(String propertyName, Object object, JsonGenerator out) throws IOException {

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartObject();
        encodeObjectBody(object, out);
        out.writeEndObject();
    }

    protected void encodeObjectBody(Object object, JsonGenerator out) throws IOException {
        int count = resultEncoder.encodeAndGetTotal(resultProperty, object, out);
        totalEncoder.encode(totalProperty, count, out);
    }
}
