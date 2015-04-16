package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.DataObjectProcessor;
import com.nhl.link.rest.runtime.parser.EmptyMultiValuedMap;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.update.UpdateFilter;

/**
 * @since 1.11
 */
public class SenchaRequestParser extends RequestParser {

	static final Pattern DASH_ID_PATTERN = Pattern.compile(".-[\\d]+$");

	static final String FILTER = "filter";

	private ISenchaFilterProcessor senchaFilterProcessor;

	public SenchaRequestParser(@Inject(UPDATE_FILTER_LIST) List<UpdateFilter> updateFilters,
			@Inject IMetadataService metadataService, @Inject IJacksonService jacksonService,
			@Inject IRelationshipMapper associationHandler, @Inject ITreeProcessor treeProcessor,
			@Inject ISortProcessor sortProcessor, @Inject IJsonValueConverterFactory jsonValueConverterFactory,
			@Inject ICayenneExpProcessor cayenneExpProcessor, @Inject IKeyValueExpProcessor keyValueExpProcessor,
			@Inject ISenchaFilterProcessor senchaFilterProcessor) {
		
		super(updateFilters, metadataService, jacksonService, associationHandler, treeProcessor, sortProcessor,
				jsonValueConverterFactory, cayenneExpProcessor, keyValueExpProcessor);

		this.senchaFilterProcessor = senchaFilterProcessor;
	}

	@Override
	protected DataObjectProcessor createObjectProcessor(IJacksonService jacksonService,
			IRelationshipMapper associationHandler, IJsonValueConverterFactory jsonValueConverterFactory) {
		return new SenchaDataObjectProcessor(DASH_ID_PATTERN, jacksonService, associationHandler,
				jsonValueConverterFactory);
	}

	@Override
	public void parseSelect(SelectContext<?> context) {
		super.parseSelect(context);

		UriInfo uriInfo = context.getUriInfo();
		MultivaluedMap<String, String> parameters = uriInfo != null ? uriInfo.getQueryParameters()
				: EmptyMultiValuedMap.map();

		Expression e1 = parseFilter(context, parameters);
		if (e1 != null) {
			context.getResponse().getEntity().andQualifier(e1);
		}
	}

	protected Expression parseFilter(SelectContext<?> context, MultivaluedMap<String, String> parameters) {
		String value = string(parameters, FILTER);
		return senchaFilterProcessor.process(context.getResponse().getEntity().getLrEntity(), value);
	}

}
