package com.nhl.link.rest.runtime.parser.size;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;

/**
 * @since 2.13
 */
public interface ISizeConstructor {

    void construct(ResourceEntity<?> resourceEntity, Start start, Limit limit);

}
