package com.nhl.link.rest.runtime.parser.size;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Query;

public class SizeProcessor implements ISizeProcessor {

    private StartConverter startConverter;
    private LimitConverter limitConverter;

    public SizeProcessor() {
        this.startConverter = new StartConverter();
        this.limitConverter = new LimitConverter();
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, Integer start, Integer limit) {
        resourceEntity.setFetchOffset(start != null ? start : -1);
        resourceEntity.setFetchLimit(limit != null ? limit : -1);
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, Query query) {
        if (query.getStart() != null) {
            resourceEntity.setFetchOffset(query.getStart().getValue());
        }
        if (query.getLimit() != null) {
            resourceEntity.setFetchLimit(query.getLimit().getValue());
        }
    }

    @Override
    public StartConverter getStartConverter() {
        return this.startConverter;
    }

    @Override
    public LimitConverter getLimitConverter() {
        return this.limitConverter;
    }
}
