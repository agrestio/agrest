package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.size.ISizeProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByProcessor;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Exclude;
import com.nhl.link.rest.runtime.query.Include;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.MapBy;
import com.nhl.link.rest.runtime.query.Sort;
import com.nhl.link.rest.runtime.query.Start;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;


public class RequestParser implements IRequestParser {
    
    private IIncludeProcessor includeProcessor;
    private IExcludeProcessor excludeProcessor;
    private ISortProcessor sortProcessor;
    private ICayenneExpProcessor cayenneExpProcessor;
    private IMapByProcessor mapByProcessor;
    private ISizeProcessor sizeProcessor;

    public RequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor,
            @Inject IMapByProcessor mapByProcessor,
            @Inject ISizeProcessor sizeProcessor) {

        this.includeProcessor = includeProcessor;
        this.excludeProcessor = excludeProcessor;
        this.sortProcessor = sortProcessor;
        this.cayenneExpProcessor = cayenneExpProcessor;
        this.mapByProcessor = mapByProcessor;
        this.sizeProcessor = sizeProcessor;
    }

    @Override
    public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {

        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        sizeProcessor.process(resourceEntity, BaseRequestProcessor.integer(protocolParameters, Start.getName()),
                                                BaseRequestProcessor.integer(protocolParameters, Limit.getName()));

        // process even if no parameters exist ... this will result in
        // default includes
        includeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Include.getName()));
        excludeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Exclude.getName()));

        sortProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, Sort.getName()),
                                                BaseRequestProcessor.string(protocolParameters, "dir"));

        mapByProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, MapBy.getName()));

        cayenneExpProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, CayenneExp.getName()));

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // process even if no parameters exist ... this will result in
        // default includes
        includeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Include.getName()));
        excludeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Exclude.getName()));

        return resourceEntity;
    }
}
