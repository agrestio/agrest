package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.encoder.Encoder;

public interface IEncoderService {

	/**
	 * Builds a hierarchical data encoder for a given response.
	 * 
	 * @since 1.20
	 */
	<T> Encoder dataEncoder(ResourceEntity<T> entity);

	/**
	 * Builds a metadata encoder for a given response.
	 * 
	 * @since 1.20
	 */
	<T> Encoder metadataEncoder(RootResourceEntity<T> entity);
}
