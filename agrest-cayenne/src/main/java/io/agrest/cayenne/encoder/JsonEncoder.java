package io.agrest.cayenne.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.AbstractEncoder;
import io.agrest.encoder.Encoder;
import org.apache.cayenne.value.Json;

import java.io.IOException;

/**
 * @since 4.1
 */
public class JsonEncoder extends AbstractEncoder {

    private static final Encoder instance = new JsonEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private JsonEncoder() {
    }

    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        Json json = (Json) object;

        // note that we can't use ValueEncoder for rg.apache.cayenne.value.Json, as it needs to write a raw value
        // without string escaping
        out.writeRawValue(json.getRawJson());
    }
}
