package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ISODateEncoder;
import com.nhl.link.rest.encoder.ISODateTimeEncoder;
import com.nhl.link.rest.encoder.ISOLocalDateEncoder;
import com.nhl.link.rest.encoder.ISOLocalDateTimeEncoder;
import com.nhl.link.rest.encoder.ISOLocalTimeEncoder;
import com.nhl.link.rest.encoder.ISOOffsetDateTimeEncoder;
import com.nhl.link.rest.encoder.ISOTimeEncoder;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static com.nhl.link.rest.meta.Types.typeForName;

public class AttributeEncoderFactoryProvider implements Provider<IAttributeEncoderFactory> {

    private Map<String, Encoder> injectedEncoders;

    public AttributeEncoderFactoryProvider(@Inject Map<String, Encoder> injectedEncoders) {
        this.injectedEncoders = injectedEncoders;
    }

    @Override
    public IAttributeEncoderFactory get() throws DIRuntimeException {
        Map<Class<?>, Encoder> encoders =
                appendInjectedEncoders(
                        appendKnownEncoders(new HashMap<>()));

        return createFactory(encoders, defaultEncoder());
    }

    /**
     * @since 2.11
     */
    protected IAttributeEncoderFactory createFactory(Map<Class<?>, Encoder> encoders, Encoder defaultEncoder) {
        return new AttributeEncoderFactory(encoders, defaultEncoder);
    }

    protected Encoder defaultEncoder() {
        return GenericEncoder.encoder();
    }

    protected Map<Class<?>, Encoder> appendKnownEncoders(Map<Class<?>, Encoder> encoders) {
        encoders.put(AttributeEncoderFactory.LOCAL_DATE, ISOLocalDateEncoder.encoder());
        encoders.put(AttributeEncoderFactory.LOCAL_TIME, ISOLocalTimeEncoder.encoder());
        encoders.put(AttributeEncoderFactory.LOCAL_DATETIME, ISOLocalDateTimeEncoder.encoder());
        encoders.put(AttributeEncoderFactory.OFFSET_DATETIME, ISOOffsetDateTimeEncoder.encoder());
        encoders.put(AttributeEncoderFactory.UTIL_DATE, ISODateTimeEncoder.encoder());
        encoders.put(AttributeEncoderFactory.SQL_TIMESTAMP, ISODateTimeEncoder.encoder());
        encoders.put(AttributeEncoderFactory.SQL_DATE, ISODateEncoder.encoder());
        encoders.put(AttributeEncoderFactory.SQL_TIME, ISOTimeEncoder.encoder());

        return encoders;
    }

    protected Map<Class<?>, Encoder> appendInjectedEncoders(Map<Class<?>, Encoder> encoders) {
        injectedEncoders.forEach((k, v) -> encoders.put(typeForName(k), v));
        return encoders;
    }
}
