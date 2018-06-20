package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;

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
     * @since 2.5
     */
    void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters);
}
