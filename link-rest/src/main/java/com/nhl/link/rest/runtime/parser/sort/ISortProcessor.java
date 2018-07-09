package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.IQueryProcessor;

/**
 * @since 1.5
 */
public interface ISortProcessor extends IQueryProcessor {

    /**
     * @since 2.5
     */
    void process(ResourceEntity<?> entity, String sort, String direction);


    void process(ResourceEntity<?> entity, JsonNode sortNode);
}
