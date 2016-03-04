package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;

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
	<T> Encoder metadataEncoder(ResourceEntity<T> entity);
}
