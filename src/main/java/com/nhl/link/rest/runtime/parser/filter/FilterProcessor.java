package com.nhl.link.rest.runtime.parser.filter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;

/**
 * @since 1.5
 */
public class FilterProcessor extends BaseRequestProcessor implements IFilterProcessor {

	private static final String CAYENNE_EXP = "cayenneExp";

	// TODO: the name of this key is a Sencha holdover.. make it configurable
	// and keep "query" under Sencha adapter
	private static final String BY_PROPERTY = "query";

	private CayenneExpProcessor cayenneExpProcessor;
	private ByPropertyProcessor byPropertyProcessor;

	public FilterProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		this.cayenneExpProcessor = new CayenneExpProcessor(jsonParser, pathCache);
		this.byPropertyProcessor = new ByPropertyProcessor();
	}

	@Override
	public void process(DataResponse<?> response, UriInfo uriInfo) {
		if (uriInfo != null) {
			MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

			// filters are chained to SelectQuery using AND, so the order is not
			// important...
			cayenneExpProcessor.process(response.getEntity(), string(parameters, CAYENNE_EXP));

			// if no property is specified, then we don't care about 'query'
			// parameter... E.g. query may have been processed by a custom code
			// outside linkrest
			if (response.getQueryProperty() != null) {
				byPropertyProcessor.process(response.getEntity(), string(parameters, BY_PROPERTY),
						response.getQueryProperty());
			}

		}
	}

	@Override
	public void process(ResourceEntity<?> entity, JsonNode expNode) {
		cayenneExpProcessor.process(entity, expNode);
	}

}
