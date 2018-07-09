package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IMapByProcessor;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

import static com.nhl.link.rest.Term.CAYENNE_EXP;
import static com.nhl.link.rest.Term.DIR;
import static com.nhl.link.rest.Term.EXCLUDE;
import static com.nhl.link.rest.Term.INCLUDE;
import static com.nhl.link.rest.Term.LIMIT;
import static com.nhl.link.rest.Term.MAP_BY;
import static com.nhl.link.rest.Term.SORT;
import static com.nhl.link.rest.Term.START;

public class RequestParser implements IRequestParser {
    
    private IIncludeProcessor includeProcessor;
    private IExcludeProcessor excludeProcessor;
    private ISortProcessor sortProcessor;
    private ICayenneExpProcessor cayenneExpProcessor;
    private IMapByProcessor mapByProcessor;

    public RequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor,
            @Inject IMapByProcessor mapByProcessor) {

        this.includeProcessor = includeProcessor;
        this.excludeProcessor = excludeProcessor;
        this.sortProcessor = sortProcessor;
        this.cayenneExpProcessor = cayenneExpProcessor;
        this.mapByProcessor = mapByProcessor;
    }

    @Override
    public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {

        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // TODO: "ISizeProcessor"
        resourceEntity.setFetchOffset(BaseRequestProcessor.integer(protocolParameters, START));
        resourceEntity.setFetchLimit(BaseRequestProcessor.integer(protocolParameters, LIMIT));

        // process even if no parameters exist ... this will result in
        // default includes
        includeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, INCLUDE));
        excludeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, EXCLUDE));

        sortProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, SORT),
                                                BaseRequestProcessor.string(protocolParameters, DIR));

        mapByProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, MAP_BY));

        cayenneExpProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, CAYENNE_EXP));

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // process even if no parameters exist ... this will result in
        // default includes
        includeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, INCLUDE));
        excludeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, EXCLUDE));

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> plainParameters, Query complexParameters) {
        if (complexParameters == null) {
            return parseSelect(entity, plainParameters);
        }

        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // TODO: "ISizeProcessor"
        resourceEntity.setFetchOffset(BaseRequestProcessor.integer(plainParameters, START));
        resourceEntity.setFetchLimit(BaseRequestProcessor.integer(plainParameters, LIMIT));

        includeProcessor.process(resourceEntity, complexParameters);
        excludeProcessor.process(resourceEntity, complexParameters);

        sortProcessor.process(resourceEntity, complexParameters);
        mapByProcessor.process(resourceEntity, BaseRequestProcessor.string(plainParameters, MAP_BY));


        cayenneExpProcessor.process(resourceEntity, complexParameters);

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> plainParameters, Query complexParameters) {
        if (complexParameters == null) {
            return parseUpdate(entity, plainParameters);
        }

        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // process even if no parameters exist ... this will result in
        // default includes
        includeProcessor.process(resourceEntity, complexParameters);
        excludeProcessor.process(resourceEntity, complexParameters);

        return resourceEntity;
    }

}
