package io.agrest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.AbstractEncoder;
import io.agrest.encoder.Encoder;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
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

