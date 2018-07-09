package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.query.Query;

import java.util.List;
import java.util.Map;

/**
 * Defines protocol adapter between the REST interface and LinkRest backend.
 */
public interface IRequestParser {

    /**
     * @since 2.5
     */
    <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters);

    /**
     * Parses request control parameters, creating a {@link ResourceEntity}, representing updating client request.
     *
     * @since 2.5
     */
    <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> protocolParameters);


    /**
     * @since 2.13
     */
    <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> plainParameters, Query complexParameters);

    /**
     * @since 2.13
     */
    <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> plainParameters, Query complexParameters);

}
