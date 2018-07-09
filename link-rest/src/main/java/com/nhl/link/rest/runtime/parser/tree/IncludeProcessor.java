package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Include;
import com.nhl.link.rest.runtime.query.Query;
import com.nhl.link.rest.runtime.query.Sort;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ParamConverter;
import java.util.List;


public class IncludeProcessor implements IIncludeProcessor {

	private IncludeConverter converter;

	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor expProcessor;
	private IMapByProcessor mapByProcessor;

	public IncludeProcessor(
	        @Inject IJacksonService jsonParser,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor expProcessor,
			@Inject IMapByProcessor mapByProcessor) {

		this.converter = new IncludeConverter(jsonParser, expProcessor, sortProcessor);
		this.sortProcessor = sortProcessor;
		this.expProcessor = expProcessor;
		this.mapByProcessor = mapByProcessor;
	}

	@Override
	public void process(ResourceEntity<?> resourceEntity, List<String> values) {
		for (String value : values) {
			Include include = converter.fromString(value);
			processOne(resourceEntity, include);
		}

		BaseRequestProcessor.processDefaultIncludes(resourceEntity);
	}

	/**
	 * @since 2.13
	 */
	@Override
	public void process(ResourceEntity<?> resourceEntity, Query query) {
		for (Include include : query.getInclude()) {
			processOne(resourceEntity, include);
		}

		BaseRequestProcessor.processDefaultIncludes(resourceEntity);
	}

	/**
	 * @since 2.13
	 */
	@Override
	public ParamConverter<?> getConverter() {
		return converter;
	}

	private void processOne(ResourceEntity<?> resourceEntity, Include include) {
		processIncludeObject(resourceEntity, include);
		// processes nested includes
		if (include != null) {
			include.getIncludes().stream().forEach(i -> processIncludeObject(resourceEntity, i));
		}
	}

	private void processIncludeObject(ResourceEntity<?> rootEntity, Include include) {
		if (include != null) {

			ResourceEntity<?> includeEntity;

			final String value = include.getValue();
			if (value != null && !value.isEmpty()) {
				BaseRequestProcessor.processIncludePath(rootEntity, value);
			}

			final String path = include.getPath();
			if (path == null || path.isEmpty()) {
				// root node
				includeEntity = rootEntity;
			} else {
				includeEntity = BaseRequestProcessor.processIncludePath(rootEntity, path);
				if (includeEntity == null) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Bad include spec, non-relationship 'path' in include object: " + path);
				}
			}

			final String mapBy = include.getMapBy();
			if (mapBy != null && !mapBy.isEmpty()) {
				mapByProcessor.processInclude(includeEntity, mapBy);
			}

			final Sort sort = include.getSort();
			if (sort != null) {
				Query query = new Query();
				query.setSort(sort);
				sortProcessor.process(includeEntity, query);
			}

			final CayenneExp cayenneExp = include.getCayenneExp();
			if (cayenneExp != null) {
				Query query = new Query();
				query.setCayenneExp(cayenneExp);
				expProcessor.process(includeEntity, query);
			}

			final Integer start = include.getStart();
			if (start != null) {
				includeEntity.setFetchOffset(start);
			}

			final Integer limit = include.getLimit();
			if (limit != null) {
				includeEntity.setFetchLimit(limit);
			}
		}
	}
}
