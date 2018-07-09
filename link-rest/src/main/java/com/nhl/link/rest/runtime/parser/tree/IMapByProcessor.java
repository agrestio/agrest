package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;

/**
 * @since 2.13
 */
public interface IMapByProcessor {

    void processInclude(ResourceEntity<?> resourceEntity, String mapByPath);

    void process(ResourceEntity<?> resourceEntity, String mapByPath);
}
