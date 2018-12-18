package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;

public interface IEncoderService {

	/**
	 * Builds a hierarchical data encoder for a given response.
	 * 
	 * @since 1.20
	 */
	<T, E> Encoder dataEncoder(ResourceEntity<T, E> entity);

	/**
	 * Builds a metadata encoder for a given response.
	 * 
	 * @since 1.20
	 */
	<T, E> Encoder metadataEncoder(ResourceEntity<T, E> entity);
}
