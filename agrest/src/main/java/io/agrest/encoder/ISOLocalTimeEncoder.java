package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.LocalTime;

import static io.agrest.encoder.DateTimeFormatters.isoLocalTime;

public class ISOLocalTimeEncoder extends AbstractEncoder {

    private static final Encoder instance = new ISOLocalTimeEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private ISOLocalTimeEncoder() {
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        LocalTime time = (LocalTime) object;
        String formatted = isoLocalTime().format(time);
        out.writeObject(formatted);
        return true;
    }

}
