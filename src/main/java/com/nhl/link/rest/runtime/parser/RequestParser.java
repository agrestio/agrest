package com.nhl.link.rest.runtime.parser;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.update.UpdateFilter;

public class RequestParser implements IRequestParser {

	public static final String UPDATE_FILTER_LIST = "linkrest.update.filter.list";

	static final String START = "start";
	static final String LIMIT = "limit";

	private ITreeProcessor treeProcessor;
	private IMetadataService metadataService;
	private ISortProcessor sortProcessor;
	private IFilterProcessor filterProcessor;

	private DataObjectProcessor dataObjectProcessor;

	private List<UpdateFilter> updateFilters;

	public RequestParser(@Inject(UPDATE_FILTER_LIST) List<UpdateFilter> updateFilters,
			@Inject IMetadataService metadataService, @Inject IJacksonService jacksonService,
			@Inject IRelationshipMapper associationHandler, @Inject ITreeProcessor treeProcessor,
			@Inject ISortProcessor sortProcessor, @Inject IFilterProcessor filterProcessor,
			@Inject IJsonValueConverterFactory jsonValueConverterFactory) {

		this.updateFilters = updateFilters;
		this.metadataService = metadataService;
		this.filterProcessor = filterProcessor;
		this.sortProcessor = sortProcessor;
		this.treeProcessor = treeProcessor;
		this.dataObjectProcessor = createObjectProcessor(jacksonService, associationHandler, jsonValueConverterFactory);
	}

	protected DataObjectProcessor createObjectProcessor(IJacksonService jacksonService,
			IRelationshipMapper associationHandler, IJsonValueConverterFactory jsonValueConverterFactory) {
		return new DataObjectProcessor(jacksonService, associationHandler, jsonValueConverterFactory);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void parseSelect(SelectContext<?> context) {

		if (context == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null context");
		}

		LrEntity<?> entity = metadataService.getLrEntity(context.getType());

		DataResponse<?> response = context.getResponse();

		ResourceEntity rootDescriptor = new ResourceEntity(entity);
		response.resourceEntity(rootDescriptor);
		response.withQueryProperty(context.getAutocompleteProperty());

		// selectById can send us a null uriInfo; still we want to run through
		// the processors in this case to init the defaults

		UriInfo uriInfo = context.getUriInfo();
		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		// TODO: "ISizeProcessor"
		response.withFetchOffset(BaseRequestProcessor.integer(parameters, START));
		response.withFetchLimit(BaseRequestProcessor.integer(parameters, LIMIT));

		treeProcessor.process(response, uriInfo);
		sortProcessor.process(response, uriInfo);
		filterProcessor.process(response, uriInfo);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void parseUpdate(UpdateContext<?> context) {
		if (context == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null context");
		}

		LrEntity<?> entity = metadataService.getLrEntity(context.getType());
		ResourceEntity resourceEntity = new ResourceEntity(entity);

		UpdateResponse<?> response = context.getResponse();

		response.resourceEntity(resourceEntity);

		treeProcessor.process(response, context.getUriInfo());

		dataObjectProcessor.process(response, context.getEntityData());

		for (UpdateFilter f : updateFilters) {
			response = f.afterParse(response);
		}
	}

}
