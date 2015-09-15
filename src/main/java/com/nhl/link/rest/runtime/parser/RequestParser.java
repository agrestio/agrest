package com.nhl.link.rest.runtime.parser;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;

public class RequestParser implements IRequestParser {

	static final String START = "start";
	static final String LIMIT = "limit";
	private static final String CAYENNE_EXP = "cayenneExp";

	// TODO: the name of this key is a Sencha holdover.. make it configurable
	// and keep "query" under Sencha adapter
	private static final String QUERY = "query";

	private ITreeProcessor treeProcessor;
	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor cayenneExpProcessor;
	private IKeyValueExpProcessor keyValueExpProcessor;

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

	public RequestParser(@Inject ITreeProcessor treeProcessor, @Inject ISortProcessor sortProcessor,
			@Inject ICayenneExpProcessor cayenneExpProcessor, @Inject IKeyValueExpProcessor keyValueExpProcessor) {

		this.sortProcessor = sortProcessor;
		this.treeProcessor = treeProcessor;
		this.cayenneExpProcessor = cayenneExpProcessor;
		this.keyValueExpProcessor = keyValueExpProcessor;
	}

	@Override
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, UriInfo uriInfo, String autocompleteProperty) {

		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

		// selectById can send us a null uriInfo; still we want to run through
		// the processors in this case to init the defaults

		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		// TODO: "ISizeProcessor"
		resourceEntity.setFetchOffset(BaseRequestProcessor.integer(parameters, START));
		resourceEntity.setFetchLimit(BaseRequestProcessor.integer(parameters, LIMIT));

		treeProcessor.process(resourceEntity, uriInfo);
		sortProcessor.process(resourceEntity, uriInfo);

		Expression e1 = parseCayenneExp(entity, parameters);
		Expression e2 = parseKeyValueExp(entity, parameters, autocompleteProperty);
		resourceEntity.andQualifier(combine(e1, e2));

		return resourceEntity;
	}

	@Override
	public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, UriInfo uriInfo) {
		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);
		treeProcessor.process(resourceEntity, uriInfo);
		return resourceEntity;
	}

	protected Expression parseCayenneExp(LrEntity<?> entity, MultivaluedMap<String, String> parameters) {
		String exp = string(parameters, CAYENNE_EXP);
		return cayenneExpProcessor.process(entity, exp);
	}

	protected Expression parseKeyValueExp(LrEntity<?> entity, MultivaluedMap<String, String> parameters,
			String autocompleteProperty) {
		String value = string(parameters, QUERY);
		return keyValueExpProcessor.process(entity, autocompleteProperty, value);
	}

}
