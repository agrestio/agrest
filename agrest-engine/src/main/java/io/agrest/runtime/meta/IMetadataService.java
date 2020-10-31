package io.agrest.runtime.meta;

import io.agrest.meta.AgEntity;

/**
 * Provides access to Agrest entity metadata.
 */
public interface IMetadataService {

    /**
     * @since 1.12
     */
    <T> AgEntity<T> getAgEntity(Class<T> type);
}
