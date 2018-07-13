package com.nhl.link.rest.runtime.parser.mapBy;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;
import com.nhl.link.rest.runtime.query.MapBy;

/**
 * @since 2.13
 */
public interface IMapByProcessor extends IQueryProcessor {

    void process(ResourceEntity<?> resourceEntity, String mapByPath);

    void processIncluded(ResourceEntity<?> resourceEntity, MapBy mapBy);

    MapByConverter getConverter();
}
