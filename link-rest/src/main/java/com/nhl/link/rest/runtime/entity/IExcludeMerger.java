package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Exclude;

import java.util.List;

/**
 * @since 2.13
 */
public interface IExcludeMerger {

    void merge(ResourceEntity<?> resourceEntity, List<Exclude> excludes);
}
