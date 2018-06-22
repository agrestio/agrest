package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;

import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public interface ISortProcessor {

    /**
     * @since 2.5
     */
    void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters);


    void process(ResourceEntity<?> entity, JsonNode sortNode);
}
