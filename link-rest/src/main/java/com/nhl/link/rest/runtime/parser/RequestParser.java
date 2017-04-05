package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeWorker;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestParser implements IRequestParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestParser.class);

	private static final String START = "start";
	private static final String LIMIT = "limit";
	private static final String CAYENNE_EXP = "cayenneExp";
	private static final String MAP_BY = "mapBy";

	// TODO: the name of this key is a Sencha holdover.. make it configurable
	// and keep "query" under Sencha adapter
	private static final String QUERY = "query";

	private ITreeProcessor treeProcessor;
	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor cayenneExpProcessor;
	private IKeyValueExpProcessor keyValueExpProcessor;


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
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters, String autocompleteProperty) {

		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

		// TODO: "ISizeProcessor"
		resourceEntity.setFetchOffset(BaseRequestProcessor.integer(protocolParameters, START));
		resourceEntity.setFetchLimit(BaseRequestProcessor.integer(protocolParameters, LIMIT));

		treeProcessor.process(resourceEntity, protocolParameters);
		sortProcessor.process(resourceEntity, protocolParameters);

		processMapBy(resourceEntity, protocolParameters);

		Expression e1 = parseCayenneExp(entity, protocolParameters);
		Expression e2 = parseKeyValueExp(entity, protocolParameters, autocompleteProperty);
		resourceEntity.andQualifier(combine(e1, e2));

		return resourceEntity;
	}

	@Override
	public <T> ResourceEntity<T> parseUpdate(LrEntity<T> entity, UriInfo uriInfo) {
		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);
		treeProcessor.process(resourceEntity, uriInfo);
		return resourceEntity;
	}

	private void processMapBy(ResourceEntity<?> descriptor, Map<String, List<String>> protocolParameters) {
		String mapByPath = BaseRequestProcessor.string(protocolParameters, MAP_BY);
		if (mapByPath != null) {
			LrAttribute attribute = descriptor.getLrEntity().getAttribute(mapByPath);
			if (attribute != null) {
				ResourceEntity<?> mapBy = new ResourceEntity<>(descriptor.getLrEntity());
				mapBy.getAttributes().put(attribute.getName(), attribute);
				descriptor.mapBy(mapBy, attribute.getName());
			} else {
				ResourceEntity<?> mapBy = new ResourceEntity<>(descriptor.getLrEntity());
				IncludeWorker.processIncludePath(mapBy, mapByPath);
				descriptor.mapBy(mapBy, mapByPath);
			}
		}
	}

	protected Expression parseCayenneExp(LrEntity<?> entity, Map<String, List<String>> protocolParameters) {
		String exp = BaseRequestProcessor.string(protocolParameters, CAYENNE_EXP);
		return cayenneExpProcessor.process(entity, exp);
	}

	protected Expression parseKeyValueExp(LrEntity<?> entity, Map<String, List<String>> protocolParameters,
			String autocompleteProperty) {
		String value = BaseRequestProcessor.string(protocolParameters, QUERY);
		return keyValueExpProcessor.process(entity, autocompleteProperty, value);
	}

}
