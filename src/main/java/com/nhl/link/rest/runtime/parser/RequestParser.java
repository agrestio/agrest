package com.nhl.link.rest.runtime.parser;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

public class RequestParser implements IRequestParser {

	private IncludeProcessor includeProcessor;
	private ExcludeProcessor excludeProcessor;
	private IMetadataService metadataService;
	private SortProcessor sortProcessor;
	private CayenneExpProcessor cayenneExpProcessor;
	private FilterProcessor filterProcessor;
	private DataObjectProcessor dataObjectProcessor;
	private QueryProcessor queryProcessor;

	public RequestParser(@Inject IMetadataService metadataService, @Inject IJacksonService jacksonService,
			@Inject IRelationshipMapper associationHandler) {

		this.metadataService = metadataService;

		RequestJsonParser jsonParser = new RequestJsonParser(jacksonService.getJsonFactory());

		// cache parsed paths as we have a finite number of valid paths in each
		// app model, and not having to parse them every time should save a few
		// cycles
		PathCache pathCache = new PathCache();

		this.cayenneExpProcessor = new CayenneExpProcessor(jsonParser, pathCache);
		this.sortProcessor = new SortProcessor(jsonParser, pathCache);
		this.includeProcessor = new IncludeProcessor(jsonParser, sortProcessor, cayenneExpProcessor);
		this.excludeProcessor = new ExcludeProcessor(jsonParser);
		this.filterProcessor = new FilterProcessor(jsonParser, pathCache);
		this.dataObjectProcessor = new DataObjectProcessor(jsonParser, associationHandler);
		this.queryProcessor = new QueryProcessor();
	}

	@Override
	public <T> DataResponse<T> parseSelect(DataResponse<T> response, UriInfo uriInfo, String autocompleteProperty) {

		if (response == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null response");
		}

		ObjEntity entity = metadataService.getObjEntity(response.getType());

		Entity<T> rootDescriptor = new Entity<T>(response.getType(), entity);
		response.withClientEntity(rootDescriptor);

		// selectById can send us a null uriInfo; still we want to run through
		// the processors in this case to init the defaults

		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		response.withFetchOffset(RequestParams.start.integer(parameters));
		response.withFetchLimit(RequestParams.limit.integer(parameters));

		includeProcessor.process(rootDescriptor, RequestParams.include.strings(parameters));
		excludeProcessor.process(rootDescriptor, RequestParams.exclude.strings(parameters));

		// groupers go before sorters (?)
		sortProcessor.process(rootDescriptor, RequestParams.group.string(parameters),
				RequestParams.groupDir.string(parameters));
		sortProcessor.process(rootDescriptor, RequestParams.sort.string(parameters),
				RequestParams.dir.string(parameters));

		// filter, cayenneExp, query keys all append to the qualifier... we
		// are
		// chaining them with AND, so the order is not relevant
		cayenneExpProcessor.process(rootDescriptor, RequestParams.cayenneExp.string(parameters));
		filterProcessor.process(rootDescriptor, RequestParams.filter.string(parameters));
		queryProcessor.process(rootDescriptor, RequestParams.query.string(parameters), autocompleteProperty);

		return response;
	}

	/**
	 * @since 1.3
	 */
	@Override
	public <T> UpdateResponse<T> parseUpdate(UpdateResponse<T> response, UriInfo uriInfo, String requestBody) {
		if (response == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null response");
		}

		ObjEntity entity = metadataService.getObjEntity(response.getType());
		Entity<T> clientEntity = new Entity<T>(response.getType(), entity);
		response.withClientEntity(clientEntity);

		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();
		
		includeProcessor.process(clientEntity, RequestParams.include.strings(parameters));
		excludeProcessor.process(clientEntity, RequestParams.exclude.strings(parameters));

		dataObjectProcessor.process(response, requestBody);

		return response;
	}

}
