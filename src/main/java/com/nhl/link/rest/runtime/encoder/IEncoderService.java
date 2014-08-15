package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.encoder.Encoder;

public interface IEncoderService {

	/**
	 * Builds a hierarchical encoder for a given response.
	 * 
	 * @since 1.3
	 */
	Encoder makeEncoder(DataResponse<?> response);
}
