package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeProcessor;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

public class RequestParser implements IRequestParser {

    private static final String INCLUDE = "include";
    private static final String EXCLUDE = "exclude";
    private static final String START = "start";
    private static final String LIMIT = "limit";
    private static final String CAYENNE_EXP = "cayenneExp";
    private static final String MAP_BY = "mapBy";
    
    private IIncludeProcessor includeProcessor;
    private IExcludeProcessor excludeProcessor;
    private ISortProcessor sortProcessor;
    private ICayenneExpProcessor cayenneExpProcessor;


    public RequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor) {

        this.includeProcessor = includeProcessor;
        this.excludeProcessor = excludeProcessor;
        this.sortProcessor = sortProcessor;
        this.cayenneExpProcessor = cayenneExpProcessor;
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

        sortProcessor.process(resourceEntity, protocolParameters);

        processMapBy(resourceEntity, protocolParameters);

        Expression exp = parseCayenneExp(entity, protocolParameters);
        resourceEntity.andQualifier(exp);

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
        ResourceEntity<T> resourceEntity = parseSelect(entity, plainParameters);

        if (complexParameters != null) {
            Expression exp = cayenneExpProcessor.process(entity, complexParameters.getCayenneExp());
            resourceEntity.andQualifier(exp);
        }

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> plainParameters, Query complexParameters) {
        return parseUpdate(entity, plainParameters);
    }

    private void processMapBy(ResourceEntity<?> descriptor, Map<String, List<String>> protocolParameters) {
        String mapByPath = BaseRequestProcessor.string(protocolParameters, MAP_BY);
        if (mapByPath != null) {
            LrAttribute attribute = descriptor.getLrEntity().getAttribute(mapByPath);
            if (attribute != null) {
                ResourceEntity<?> mapBy = new ResourceEntity<>(descriptor.getLrEntity());
                mapBy.getAttributes().put(attribute.getName(), attribute);
                descriptor.mapBy(mapBy, attribute.getName());
            } else {
                ResourceEntity<?> mapBy = new ResourceEntity<>(descriptor.getLrEntity());
                IncludeProcessor.processIncludePath(mapBy, mapByPath);
                descriptor.mapBy(mapBy, mapByPath);
            }
        }
    }

    protected Expression parseCayenneExp(LrEntity<?> entity, Map<String, List<String>> protocolParameters) {
        String exp = BaseRequestProcessor.string(protocolParameters, CAYENNE_EXP);
        return cayenneExpProcessor.process(entity, exp);
    }
}
