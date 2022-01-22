package io.agrest;

import java.net.URI;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public interface MetadataBuilder<T> {

    MetadataBuilder<T> forResource(Class<?> resourceClass);

    /**
     * @since 5.0
     */
    default MetadataBuilder<T> baseUri(URI baseUri) {
        return baseUri(baseUri.toString());
    }

    /**
     * @since 5.0
     */
    MetadataBuilder<T> baseUri(String baseUri);

    MetadataResponse<T> process();
}
