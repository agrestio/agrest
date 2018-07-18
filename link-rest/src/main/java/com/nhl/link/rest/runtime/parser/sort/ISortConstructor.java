package com.nhl.link.rest.runtime.parser.sort;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Sort;

/**
 * @since 2.13
 */
public interface ISortConstructor {

    /**
     * @since 2.13
     */
    void construct(ResourceEntity<?> entity, Sort sort);

}
