package com.nhl.link.rest.runtime.parser.size;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.Start;

/**
 * @since 2.13
 */
public interface ISizeConstructor {

    void construct(ResourceEntity<?> resourceEntity, Start start, Limit limit);

}
