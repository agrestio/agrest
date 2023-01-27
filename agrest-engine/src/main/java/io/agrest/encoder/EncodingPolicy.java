package io.agrest.encoder;

/**
 * A policy for JSON encoding, defining things like inclusion of null properties.
 *
 * @since 5.0
 */
public class EncodingPolicy {

    private final boolean skipNullProperties;

    public EncodingPolicy(boolean skipNullProperties) {
        this.skipNullProperties = skipNullProperties;
    }

    public boolean skipNullProperties() {
        return skipNullProperties;
    }
}
