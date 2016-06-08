package com.nhl.link.rest.runtime.parser.tree;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.EmptyMultiValuedMap;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;

/**
 * @since 1.5
 */
public class IncludeExcludeProcessor extends BaseRequestProcessor implements ITreeProcessor {

	private static final String INCLUDE = "include";
	private static final String EXCLUDE = "exclude";

	private IncludeWorker includeWorker;
	private ExcludeWorker excludeWorker;

	public IncludeExcludeProcessor(@Inject IJacksonService jacksonService, @Inject ISortProcessor sortProcessor,
			@Inject ICayenneExpProcessor expProcessor) {
		this.includeWorker = new IncludeWorker(jacksonService, sortProcessor, expProcessor);
		this.excludeWorker = new ExcludeWorker(jacksonService);
	}

	@Override
	public void process(ResourceEntity<?> entity, UriInfo uriInfo) {

		// process even if uriinfo is not available ... this will result in
		// default includes
		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		includeWorker.process(entity, strings(parameters, INCLUDE));
		excludeWorker.process(entity, strings(parameters, EXCLUDE));
	}
}
