package com.nhl.link.rest.runtime.parser.size;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;

/**
 * @since 2.13
 */
public interface ISizeProcessor extends IQueryProcessor {

    void process(ResourceEntity<?> entity, Integer start, Integer limit);

    StartConverter getStartConverter();
    LimitConverter getLimitConverter();
}
