package com.nhl.link.rest.runtime.parser.tree;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.EmptyMultiValuedMap;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
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
			@Inject IFilterProcessor filterProcessor, @Inject IMetadataService metadataService) {
		this.includeWorker = new IncludeWorker(jacksonService, sortProcessor, filterProcessor, metadataService);
		this.excludeWorker = new ExcludeWorker(jacksonService);
	}

	@Override
	public void process(DataResponse<?> response, UriInfo uriInfo) {

		// process even if uriinfo is not available ... this will result in
		// default includes
		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		includeWorker.process(response.getEntity(), strings(parameters, INCLUDE));
		excludeWorker.process(response.getEntity(), strings(parameters, EXCLUDE));
	}
}
