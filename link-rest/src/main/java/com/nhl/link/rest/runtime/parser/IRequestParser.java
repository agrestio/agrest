package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Defines protocol adapter between the REST interface and LinkRest backend.
 */
public interface IRequestParser {

    /**
     * Parses request control parameters, creating a {@link ResourceEntity}, representing client request.
     *
     * @since 1.20
     * @deprecated since 2.5 in favor of {@link #parseSelect(LrEntity, Map, String)}.
     */
    @Deprecated
    default <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, UriInfo uriInfo, String autocompleteProperty) {
        Map<String, List<String>> protocolParameters = uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
        return parseSelect(entity, protocolParameters, autocompleteProperty);
    }

    /**
     * @since 2.5
     */
    <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters, String autocompleteProperty);


    /**
     * Parses request control parameters, creating a {@link ResourceEntity}, representing updating client request.
     *
     * @since 1.20
     * @deprecated since 2.5 in favor of {@link #parseUpdate(LrEntity, Map)}.
     */
    @Deprecated
    default <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, UriInfo uriInfo) {
        Map<String, List<String>> protocolParameters = uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
        return parseUpdate(entity, protocolParameters);
    }

    /**
     * Parses request control parameters, creating a {@link ResourceEntity}, representing updating client request.
     *
     * @since 2.5
     */
    <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> protocolParameters);

}
