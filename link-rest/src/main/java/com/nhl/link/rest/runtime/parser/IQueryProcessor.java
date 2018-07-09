package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.query.Query;

import javax.ws.rs.ext.ParamConverter;

/**
 * @since 2.13
 */
public interface IQueryProcessor {

    void process(ResourceEntity<?> resourceEntity, Query query);

    ParamConverter<?> getConverter();
}
