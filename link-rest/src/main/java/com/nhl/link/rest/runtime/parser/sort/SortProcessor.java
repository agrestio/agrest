package com.nhl.link.rest.runtime.parser.sort;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;

/**
 * @since 1.5
 */
public class SortProcessor extends BaseRequestProcessor implements ISortProcessor {

	private static final String SORT = "sort";
	private static final String DIR = "dir";

	private SortWorker worker;

	public SortProcessor(@Inject IJacksonService jacksonService, @Inject IPathCache pathCache) {
		this.worker = new SortWorker(jacksonService, pathCache);
	}

	@Override
	public void process(ResourceEntity<?> entity, UriInfo uriInfo) {
		if (uriInfo != null) {
			MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
			process(entity, string(parameters, SORT), string(parameters, DIR));
		}
	}

	protected void process(ResourceEntity<?> resourceEntity, String sort, String direction) {
		worker.process(resourceEntity, sort, direction);
	}

	@Override
	public void process(ResourceEntity<?> entity, JsonNode sortNode) {
		if (sortNode.isTextual()) {
			worker.process(entity, sortNode.asText(), null);
		} else {
			worker.processSorterArray(entity, sortNode);
		}
	}

}
