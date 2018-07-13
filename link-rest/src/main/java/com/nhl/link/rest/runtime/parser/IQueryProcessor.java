package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Query;

/**
 * @since 2.13
 */
public interface IQueryProcessor {

    void process(ResourceEntity<?> resourceEntity, Query query);
}
