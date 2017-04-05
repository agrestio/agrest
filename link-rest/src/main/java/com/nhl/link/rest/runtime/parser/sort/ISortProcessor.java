package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public interface ISortProcessor {

    /**
     * @deprecated since 2.5 in fvaor of {@link #process(ResourceEntity, Map)}.
     */
    @Deprecated
    default void process(ResourceEntity<?> entity, UriInfo uriInfo) {
        if(uriInfo != null) {
            process(entity, uriInfo.getQueryParameters());
        }
    }

    /**
     * @since 2.5
     */
    void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters);


    void process(ResourceEntity<?> entity, JsonNode sortNode);
}
