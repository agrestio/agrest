package com.nhl.link.rest.runtime.adapter.sencha;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.filter.FilterProcessor;

/**
 * @since 1.5
 */
public class SenchaFilterProcessor extends FilterProcessor {

	static final String FILTER = "filter";

	private com.nhl.link.rest.runtime.adapter.sencha.FilterProcessor filterProcessor;

	public SenchaFilterProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		super(jsonParser, pathCache);
		this.filterProcessor = new com.nhl.link.rest.runtime.adapter.sencha.FilterProcessor(jsonParser, pathCache);
	}

	@Override
	public void process(DataResponse<?> response, UriInfo uriInfo) {

		if (uriInfo != null) {
			MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
			// filter, cayenneExp, query keys all append to the qualifier... we
			// are
			// chaining them with AND, so the order is not relevant

			super.process(response, uriInfo);
			filterProcessor.process(response.getEntity(), string(parameters, FILTER));
		}
	}
}
