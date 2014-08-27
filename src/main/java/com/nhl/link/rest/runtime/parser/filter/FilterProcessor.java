package com.nhl.link.rest.runtime.parser.filter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
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

	private CayenneExpProcessor cayenneExpWorker;
	private ByPropertyProcessor byPropertyWorker;

	public FilterProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		this.cayenneExpWorker = new CayenneExpProcessor(jsonParser, pathCache);
		this.byPropertyWorker = new ByPropertyProcessor();
	}

	@Override
	public void process(DataResponse<?> response, UriInfo uriInfo) {
		if (uriInfo != null) {
			MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

			// filters are chained to SelectQuery using AND, so the order is not
			// important...
			cayenneExpWorker.process(response.getEntity(), string(parameters, CAYENNE_EXP));
			byPropertyWorker
					.process(response.getEntity(), string(parameters, BY_PROPERTY), response.getQueryProperty());

		}
	}

	@Override
	public void process(Entity<?> entity, JsonNode expNode) {
		cayenneExpWorker.process(entity, expNode);
	}

}
