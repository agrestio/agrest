package io.agrest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.AbstractEncoder;
import io.agrest.encoder.Encoder;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
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
        String formatted = date.toString();
        out.writeObject(formatted);
        return true;
    }

}

