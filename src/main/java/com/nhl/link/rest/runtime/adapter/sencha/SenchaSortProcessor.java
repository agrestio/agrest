package com.nhl.link.rest.runtime.adapter.sencha;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;

/**
 * @since 1.5
 */
public class SenchaSortProcessor extends SortProcessor {

	static final String GROUP = "group";
	static final String GROUP_DIR = "groupDir";

	public SenchaSortProcessor(@Inject IJacksonService jacksonService, @Inject IPathCache pathCache) {
		super(jacksonService, pathCache);
	}

	@Override
	public void process(DataResponse<?> response, UriInfo uriInfo) {

		if (uriInfo != null) {

			// sencha groupers go before sorters (sorters are processed by
			// super).
			MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
			process(response.getEntity(), string(parameters, GROUP), string(parameters, GROUP_DIR));

			super.process(response, uriInfo);
		}
	}

}
