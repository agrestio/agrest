package io.agrest.encoder;

import java.util.Map;

/**
 * Provides access to encoders of simple values for different Java types. Can be preconfigured to use custom encoders
 * via DI.
 *
 * @since 3.3
 */
public class ValueEncoders {

    private final Map<Class<?>, Encoder> encodersByJavaType;
    private final Encoder defaultEncoder;

    public ValueEncoders(Map<Class<?>, Encoder> encodersByJavaType, Encoder defaultEncoder) {
        this.encodersByJavaType = encodersByJavaType;
        this.defaultEncoder = defaultEncoder;
    }

    public Encoder getEncoder(Class<?> javaType) {
        return encodersByJavaType.getOrDefault(javaType, defaultEncoder);
    }
}
