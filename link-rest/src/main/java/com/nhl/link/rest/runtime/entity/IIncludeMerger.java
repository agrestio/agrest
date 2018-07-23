package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Include;

import java.util.List;

/**
 * @since 2.13
 */
public interface IIncludeMerger {

    void merge(ResourceEntity<?> resourceEntity, List<Include> includes);
}
