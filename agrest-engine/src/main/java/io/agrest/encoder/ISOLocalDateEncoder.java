package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.LocalDate;

import static io.agrest.encoder.DateTimeFormatters.isoLocalDate;

public class ISOLocalDateEncoder extends AbstractEncoder {

    private static final Encoder instance = new ISOLocalDateEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private ISOLocalDateEncoder() {
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        LocalDate date = (LocalDate) object;
        String formatted = isoLocalDate().format(date);
        out.writeObject(formatted);
        return true;
    }

}
