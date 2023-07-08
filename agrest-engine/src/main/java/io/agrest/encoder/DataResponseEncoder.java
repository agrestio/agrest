package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;

import java.io.IOException;

/**
 * @since 2.1
 */
public class DataResponseEncoder implements Encoder {

    private final static DataResponseEncoder instance = withElementEncoder(GenericEncoder.encoder());

    private final String dataProperty;
    private final Encoder dataEncoder;

    private final String totalProperty;
    private final Encoder totalEncoder;

    /**
     * @since 5.0
     */
    public static DataResponseEncoder defaultEncoder() {
        return instance;
    }

    /**
     * @since 5.0
     */
    public static DataResponseEncoder withElementEncoder(Encoder elementEncoder) {
        return new DataResponseEncoder(
                "data",
                new ListEncoder(elementEncoder),
                "total",
                GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static DataResponseEncoder encoder(
            String dataProperty,
            Encoder dataEncoder,
            String totalProperty,
            Encoder totalEncoder) {

        return new DataResponseEncoder(
                dataProperty,
                dataEncoder,
                totalProperty,
                totalEncoder);
    }

    protected DataResponseEncoder(
            String dataProperty,
            Encoder dataEncoder,
            String totalProperty,
            Encoder totalEncoder) {

        this.totalProperty = totalProperty;
        this.dataEncoder = dataEncoder;
        this.totalEncoder = totalEncoder;
        this.dataProperty = dataProperty;
    }

    @Override
    public void encode(String propertyName, Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {

        if (propertyName != null) {
            out.writeFieldName(propertyName);
        }

        out.writeStartObject();
        encodeObjectBody((DataResponse<?>) object, skipNullProperties, out);
        out.writeEndObject();
    }

    protected void encodeObjectBody(DataResponse<?> response, boolean skipNullProperties, JsonGenerator out) throws IOException {
        dataEncoder.encode(dataProperty, response.getData(), skipNullProperties, out);
        totalEncoder.encode(totalProperty, response.getTotal(), skipNullProperties, out);
    }
}
