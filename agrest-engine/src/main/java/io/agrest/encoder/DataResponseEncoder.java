package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @since 2.1
 */
public class DataResponseEncoder implements Encoder {

    private CollectionEncoder resultEncoder;
    private Encoder totalEncoder;
    private String resultProperty;
    private String totalProperty;

    public DataResponseEncoder(String resultProperty, CollectionEncoder resultEncoder, String totalProperty, Encoder totalEncoder) {
        this.totalProperty = totalProperty;
        this.resultEncoder = resultEncoder;
        this.totalEncoder = totalEncoder;
        this.resultProperty = resultProperty;
    }

    @Override
    public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartObject();

        encodeObjectBody(object, out);

        out.writeEndObject();
        return true;
    }

    protected void encodeObjectBody(Object object, JsonGenerator out) throws IOException {
        int count = resultEncoder.encodeAndGetTotal(resultProperty, object, out);
        totalEncoder.encode(totalProperty, count, out);
    }

    @Override
    public int visitEntities(Object object, EncoderVisitor visitor) {
        return resultEncoder.visitEntities(object, visitor);
    }
}
