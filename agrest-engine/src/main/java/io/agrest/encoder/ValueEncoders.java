package io.agrest.encoder;

import io.agrest.encoder.Encoder;

import java.util.Map;

/**
 * Provides access to value encoders for different Java types. Can be preconfigured to use custom encoders via DI.
 *
 * @since 3.3
 */
public class ValueEncoders {

    private Map<Class<?>, Encoder> encodersByJavaType;
    private Encoder defaultEncoder;

    public ValueEncoders(Map<Class<?>, Encoder> encodersByJavaType, Encoder defaultEncoder) {
        this.encodersByJavaType = encodersByJavaType;
        this.defaultEncoder = defaultEncoder;
    }

    public Encoder getEncoder(Class<?> javaType) {
        return encodersByJavaType.getOrDefault(javaType, defaultEncoder);
    }
}
