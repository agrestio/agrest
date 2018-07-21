package com.nhl.link.rest.runtime.parser.sort;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Sort;

/**
 * @since 2.13
 */
public interface ISortConstructor {

    void construct(ResourceEntity<?> entity, Sort sort);
}
