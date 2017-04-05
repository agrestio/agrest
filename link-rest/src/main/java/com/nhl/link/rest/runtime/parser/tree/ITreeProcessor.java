package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.EmptyMultiValuedMap;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * Processes include/exclude property keys from the request, constructing a
 * matching response entity hierarchy.
 *
 * @since 1.5
 */
public interface ITreeProcessor {

    /**
     * @deprecated since 2.5 in favor of {@link #process(ResourceEntity, Map)}.
     */
    @Deprecated
    default void process(ResourceEntity<?> entity, UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = uriInfo != null
                ? uriInfo.getQueryParameters()
                : EmptyMultiValuedMap.map();

        process(entity, parameters);
    }

    /**
     * @since 2.5
     */
    void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters);
}
