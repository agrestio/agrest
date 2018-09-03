package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.LocalDateTime;

import static io.agrest.encoder.DateTimeFormatters.isoLocalDateTime;

public class ISOLocalDateTimeEncoder extends AbstractEncoder {

    private static final Encoder instance = new ISOLocalDateTimeEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private ISOLocalDateTimeEncoder() {
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        LocalDateTime dateTime = (LocalDateTime) object;
        String formatted = isoLocalDateTime().format(dateTime);
        out.writeObject(formatted);
        return true;
    }

}
