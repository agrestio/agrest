package com.nhl.link.rest.runtime.config;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConfig;

/**
 * @since 1.1
 */
public interface IConfigMerger {

	/**
	 * Updates target DataResponse with parameters from source config with
	 * override policies defined by the nature of a particular
	 * {@link IConfigMerger} implementation.
	 */
	void merge(DataResponseConfig source, DataResponse<?> target);
}
