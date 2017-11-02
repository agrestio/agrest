package com.nhl.link.rest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.AbstractEncoder;
import com.nhl.link.rest.encoder.Encoder;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

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
        String formatted = time.truncatedTo(ChronoUnit.SECONDS).toString();
        out.writeObject(formatted);
        return true;
    }

}

