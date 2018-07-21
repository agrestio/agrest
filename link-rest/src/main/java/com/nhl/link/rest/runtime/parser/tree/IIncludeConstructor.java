package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Include;

import java.util.List;

/**
 * @since 2.13
 */
public interface IIncludeConstructor {

    void construct(ResourceEntity<?> resourceEntity, List<Include> includes);
}
