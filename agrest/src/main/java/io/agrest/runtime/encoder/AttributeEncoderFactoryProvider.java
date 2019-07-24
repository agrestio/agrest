package io.agrest.runtime.encoder;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ISODateEncoder;
import io.agrest.encoder.ISODateTimeEncoder;
import io.agrest.encoder.ISOLocalDateEncoder;
import io.agrest.encoder.ISOLocalDateTimeEncoder;
import io.agrest.encoder.ISOLocalTimeEncoder;
import io.agrest.encoder.ISOOffsetDateTimeEncoder;
import io.agrest.encoder.ISOTimeEncoder;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.meta.Types.typeForName;

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
        encoders.put(PropertyTypes.LOCAL_DATE, ISOLocalDateEncoder.encoder());
        encoders.put(PropertyTypes.LOCAL_TIME, ISOLocalTimeEncoder.encoder());
        encoders.put(PropertyTypes.LOCAL_DATETIME, ISOLocalDateTimeEncoder.encoder());
        encoders.put(PropertyTypes.OFFSET_DATETIME, ISOOffsetDateTimeEncoder.encoder());
        encoders.put(PropertyTypes.UTIL_DATE, ISODateTimeEncoder.encoder());
        encoders.put(PropertyTypes.SQL_TIMESTAMP, ISODateTimeEncoder.encoder());
        encoders.put(PropertyTypes.SQL_DATE, ISODateEncoder.encoder());
        encoders.put(PropertyTypes.SQL_TIME, ISOTimeEncoder.encoder());

        return encoders;
    }

    protected Map<Class<?>, Encoder> appendInjectedEncoders(Map<Class<?>, Encoder> encoders) {
        injectedEncoders.forEach((k, v) -> encoders.put(typeForName(k), v));
        return encoders;
    }
}
