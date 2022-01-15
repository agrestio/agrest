package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.processor.ProcessingContext;

public interface IEncoderService {

    /**
     * Builds a hierarchical data encoder for a given resource entity.
     *
     * @since 5.0
     */
    <T> Encoder dataEncoder(ResourceEntity<T> entity, ProcessingContext<T> context);

    /**
     * Builds a metadata encoder for a given response.
     *
     * @since 1.20
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    <T> Encoder metadataEncoder(RootResourceEntity<T> entity);
}
