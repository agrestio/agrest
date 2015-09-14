package com.nhl.link.rest.runtime.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

public class RequestParser implements IRequestParser {

	static final String START = "start";
	static final String LIMIT = "limit";
	private static final String CAYENNE_EXP = "cayenneExp";

	// TODO: the name of this key is a Sencha holdover.. make it configurable
	// and keep "query" under Sencha adapter
	private static final String QUERY = "query";

	private ITreeProcessor treeProcessor;
	private IMetadataService metadataService;
	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor cayenneExpProcessor;
	private IKeyValueExpProcessor keyValueExpProcessor;
	private IUpdateParser updateParser;
	private IJacksonService jacksonService;

	protected static String string(MultivaluedMap<String, String> parameters, String name) {
		return parameters.getFirst(name);
	}

	protected static List<String> strings(MultivaluedMap<String, String> parameters, String name) {
		List<String> result = parameters.get(name);
		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

	protected static int integer(MultivaluedMap<String, String> parameters, String name) {

		String value = parameters.getFirst(name);
		if (value == null) {
			return -1;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfex) {
			return -1;
		}
	}

	protected static Expression combine(Expression e1, Expression e2) {
		return e1 == null ? e2 : (e2 == null) ? e1 : e1.andExp(e2);
	}

	public RequestParser(@Inject IMetadataService metadataService, @Inject IJacksonService jacksonService,
			@Inject ITreeProcessor treeProcessor, @Inject ISortProcessor sortProcessor,
			@Inject IUpdateParser updateParser, @Inject ICayenneExpProcessor cayenneExpProcessor,
			@Inject IKeyValueExpProcessor keyValueExpProcessor) {

		this.metadataService = metadataService;
		this.sortProcessor = sortProcessor;
		this.treeProcessor = treeProcessor;
		this.cayenneExpProcessor = cayenneExpProcessor;
		this.keyValueExpProcessor = keyValueExpProcessor;
		this.updateParser = updateParser;
		this.jacksonService = jacksonService;
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

		Expression e1 = parseCayenneExp(context, parameters);
		Expression e2 = parseKeyValueExp(context, parameters);
		response.getEntity().andQualifier(combine(e1, e2));
	}

	protected Expression parseCayenneExp(SelectContext<?> context, MultivaluedMap<String, String> parameters) {
		String exp = string(parameters, CAYENNE_EXP);
		return cayenneExpProcessor.process(context.getResponse().getEntity().getLrEntity(), exp);
	}

	protected Expression parseKeyValueExp(SelectContext<?> context, MultivaluedMap<String, String> parameters) {
		String value = string(parameters, QUERY);
		return keyValueExpProcessor.process(context.getResponse().getEntity().getLrEntity(),
				context.getAutocompleteProperty(), value);
	}

	@Override
	public <T> void parseUpdate(UpdateContext<T> context) {
		if (context == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Null context");
		}

		LrEntity<T> entity = metadataService.getLrEntity(context.getType());
		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

		DataResponse<T> response = context.getResponse();
		response.resourceEntity(resourceEntity);
		treeProcessor.process(response, context.getUriInfo());

		// skip parsing if we already received EntityUpdates collection parsed
		// by MessageBodyReader

		if (context.getUpdates() == null) {
			JsonNode node = jacksonService.parseJson(context.getEntityData());
			Collection<EntityUpdate<T>> updates = updateParser.parse(context.getResponse().getEntity().getLrEntity(),
					node);
			context.setUpdates(updates);
		}
	}

}
