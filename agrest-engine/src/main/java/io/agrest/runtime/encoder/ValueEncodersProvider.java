package io.agrest.runtime.encoder;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ValueEncoder;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

public class ValueEncodersProvider implements Provider<ValueEncoders> {

    private final Map<String, Encoder> injectedEncoders;
    private final IValueJsonConverterFactory converterFactory;

    public ValueEncodersProvider(
            @Inject IValueJsonConverterFactory converterFactory,
            @Inject Map<String, Encoder> injectedEncoders) {

        this.converterFactory = converterFactory;
        this.injectedEncoders = injectedEncoders;
    }

    @Override
    public ValueEncoders get() {
        return createValueEncoders(createEncoders(), defaultEncoder());
    }

    protected ValueEncoders createValueEncoders(Map<Class<?>, Encoder> encoders, Encoder defaultEncoder) {
        return new ValueEncoders(encoders, defaultEncoder);
    }

    protected Map<Class<?>, Encoder> createEncoders() {
        Map<Class<?>, Encoder> encoders = new HashMap<>();
        appendKnownEncoders(encoders);
        appendInjectedEncoders(encoders);
        return encoders;
    }

    protected Encoder defaultEncoder() {
        return GenericEncoder.encoder();
    }

    protected void appendKnownEncoders(Map<Class<?>, Encoder> encoders) {
        converterFactory.getConverters().forEach((t, c) -> encoders.put(t, new ValueEncoder(c)));
    }

    protected void appendInjectedEncoders(Map<Class<?>, Encoder> encoders) {
        injectedEncoders.forEach((k, v) -> encoders.put(typeForName(k), v));
    }
}
