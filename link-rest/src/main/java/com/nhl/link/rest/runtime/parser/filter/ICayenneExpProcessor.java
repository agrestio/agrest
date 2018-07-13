package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;

/**
 * @since 1.17
 */
public interface ICayenneExpProcessor extends IQueryProcessor {

	void process(ResourceEntity<?> resourceEntity, String expressionString);

	/**
	 * @since 2.13
	 */
	CayenneExpConverter getConverter();
}
