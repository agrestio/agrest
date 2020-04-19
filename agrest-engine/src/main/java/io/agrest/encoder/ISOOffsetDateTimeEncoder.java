package io.agrest.encoder;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.core.JsonGenerator;

public class ISOOffsetDateTimeEncoder extends AbstractEncoder {

    private static final Encoder instance = new ISOOffsetDateTimeEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private ISOOffsetDateTimeEncoder() {
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        OffsetDateTime dateTime = (OffsetDateTime) object;
        String formatted = DateTimeFormatters.isoOffsetDateTime().format(dateTime);
        out.writeObject(formatted);
        return true;
    }

}