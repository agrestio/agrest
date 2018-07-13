package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;

import java.util.List;

/**
 * @since 2.13
 */
public interface IIncludeProcessor extends IQueryProcessor {

    void process(ResourceEntity<?> resourceEntity, List<String> includes);

    IncludeConverter getConverter();
}
