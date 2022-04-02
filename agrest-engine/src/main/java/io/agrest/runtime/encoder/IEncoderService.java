package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.processor.ProcessingContext;

public interface IEncoderService {

    /**
     * Builds a hierarchical data encoder for a given resource entity.
     *
     * @since 5.0
     */
    <T> Encoder dataEncoder(ResourceEntity<T> entity, ProcessingContext<T> context);
}
