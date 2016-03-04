package com.nhl.link.rest.runtime.adapter.sencha;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.EmptyMultiValuedMap;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;

/**
 * @since 1.11
 */
public class SenchaRequestParser extends RequestParser {

	static final String FILTER = "filter";

	private ISenchaFilterProcessor senchaFilterProcessor;

	public SenchaRequestParser(@Inject ITreeProcessor treeProcessor, @Inject ISortProcessor sortProcessor,
			@Inject ICayenneExpProcessor cayenneExpProcessor, @Inject IKeyValueExpProcessor keyValueExpProcessor,
			@Inject ISenchaFilterProcessor senchaFilterProcessor) {

		super(treeProcessor, sortProcessor, cayenneExpProcessor, keyValueExpProcessor);

		this.senchaFilterProcessor = senchaFilterProcessor;
	}

	@Override
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, UriInfo uriInfo, String autocompleteProperty) {
		ResourceEntity<T> resourceEntity = super.parseSelect(entity, uriInfo, autocompleteProperty);

		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		Expression e1 = parseFilter(entity, parameters);
		if (e1 != null) {
			resourceEntity.andQualifier(e1);
		}

		return resourceEntity;
	}

	protected Expression parseFilter(LrEntity<?> entity, MultivaluedMap<String, String> parameters) {
		String value = string(parameters, FILTER);
		return senchaFilterProcessor.process(entity, value);
	}

}
