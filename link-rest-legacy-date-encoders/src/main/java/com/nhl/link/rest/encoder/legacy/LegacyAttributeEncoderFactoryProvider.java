package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactoryProvider;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import org.apache.cayenne.di.Inject;

import java.util.Map;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyAttributeEncoderFactoryProvider extends AttributeEncoderFactoryProvider {

    public LegacyAttributeEncoderFactoryProvider(@Inject Map<String, Encoder> injectedEncoders) {
        super(injectedEncoders);
    }

    @Override
    protected IAttributeEncoderFactory createFactory(Map<Class<?>, Encoder> encoders, Encoder defaultEncoder) {
        return new LegacyAttributeEncoderFactory(encoders, defaultEncoder);
    }
}
