package com.nhl.link.rest.runtime.parser;

import java.util.List;

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
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
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
			@Inject ISortProcessor sortProcessor, @Inject IFilterProcessor filterProcessor) {

		this.updateFilters = updateFilters;
		this.metadataService = metadataService;
		this.filterProcessor = filterProcessor;
		this.sortProcessor = sortProcessor;
		this.treeProcessor = treeProcessor;
		this.dataObjectProcessor = new DataObjectProcessor(jacksonService, associationHandler);
	}

	@Override
	public <T> DataResponse<T> parseSelect(DataResponse<T> response, UriInfo uriInfo, String autocompleteProperty) {

		if (response == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null response");
		}

		ObjEntity entity = metadataService.getObjEntity(response.getType());

		Entity<T> rootDescriptor = new Entity<T>(response.getType(), entity);
		response.withClientEntity(rootDescriptor);
		response.withQueryProperty(autocompleteProperty);

		// selectById can send us a null uriInfo; still we want to run through
		// the processors in this case to init the defaults

		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		// TODO: "ISizeProcessor"
		response.withFetchOffset(BaseRequestProcessor.integer(parameters, START));
		response.withFetchLimit(BaseRequestProcessor.integer(parameters, LIMIT));

		treeProcessor.process(response, uriInfo);
		sortProcessor.process(response, uriInfo);
		filterProcessor.process(response, uriInfo);

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

		treeProcessor.process(response, uriInfo);

		dataObjectProcessor.process(response, requestBody);

		for (UpdateFilter f : updateFilters) {
			response = f.afterParse(response);
		}

		return response;
	}

}
