package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.CayenneExp;

/**
 * @since 2.13
 */
public interface ICayenneExpConstructor {

	void construct(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp);
}
