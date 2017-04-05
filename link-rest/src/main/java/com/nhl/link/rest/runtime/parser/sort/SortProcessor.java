package com.nhl.link.rest.runtime.parser.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public class SortProcessor extends BaseRequestProcessor implements ISortProcessor {

    private static final String SORT = "sort";
    private static final String DIR = "dir";

    private SortWorker worker;

    public SortProcessor(@Inject IJacksonService jacksonService, @Inject IPathCache pathCache) {
        this.worker = new SortWorker(jacksonService, pathCache);
    }

    @Override
    public void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters) {
        process(entity, string(protocolParameters, SORT), string(protocolParameters, DIR));
    }

    protected void process(ResourceEntity<?> resourceEntity, String sort, String direction) {
        worker.process(resourceEntity, sort, direction);
    }

    @Override
    public void process(ResourceEntity<?> entity, JsonNode sortNode) {
        if (sortNode.isTextual()) {
            worker.process(entity, sortNode.asText(), null);
        } else {
            worker.processSorterArray(entity, sortNode);
        }
    }

}
