package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Exclude;

import java.util.List;

/**
 * @since 2.13
 */
public interface IExcludeConstructor {

    void construct(ResourceEntity<?> resourceEntity, List<Exclude> excludes);
}
