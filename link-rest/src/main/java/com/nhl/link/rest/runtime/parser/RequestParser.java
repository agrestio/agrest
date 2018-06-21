package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeWorker;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

public class RequestParser implements IRequestParser {

    private static final String START = "start";
    private static final String LIMIT = "limit";
    private static final String CAYENNE_EXP = "cayenneExp";
    private static final String MAP_BY = "mapBy";

    // TODO: the name of this key is a Sencha holdover.. make it configurable
    // and keep "query" under Sencha adapter
    private static final String QUERY = "query";

    private ITreeProcessor treeProcessor;
    private ISortProcessor sortProcessor;
    private ICayenneExpProcessor cayenneExpProcessor;


    public RequestParser(
            @Inject ITreeProcessor treeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor) {

        this.sortProcessor = sortProcessor;
        this.treeProcessor = treeProcessor;
        this.cayenneExpProcessor = cayenneExpProcessor;
    }

    @Override
    public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {

        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        // TODO: "ISizeProcessor"
        resourceEntity.setFetchOffset(BaseRequestProcessor.integer(protocolParameters, START));
        resourceEntity.setFetchLimit(BaseRequestProcessor.integer(protocolParameters, LIMIT));

        treeProcessor.process(resourceEntity, protocolParameters);
        sortProcessor.process(resourceEntity, protocolParameters);

        processMapBy(resourceEntity, protocolParameters);

        Expression exp = parseCayenneExp(entity, protocolParameters);
        resourceEntity.andQualifier(exp);

        return resourceEntity;
    }

    @Override
    public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);
        treeProcessor.process(resourceEntity, protocolParameters);
        return resourceEntity;
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
                IncludeWorker.processIncludePath(mapBy, mapByPath);
                descriptor.mapBy(mapBy, mapByPath);
            }
        }
    }

    protected Expression parseCayenneExp(LrEntity<?> entity, Map<String, List<String>> protocolParameters) {
        String exp = BaseRequestProcessor.string(protocolParameters, CAYENNE_EXP);
        return cayenneExpProcessor.process(entity, exp);
    }
}
