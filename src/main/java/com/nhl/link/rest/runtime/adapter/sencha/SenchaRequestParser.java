package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.DataObjectProcessor;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.update.UpdateFilter;

/**
 * @since 1.11
 */
public class SenchaRequestParser extends RequestParser {

	static final Pattern DASH_ID_PATTERN = Pattern.compile(".-[\\d]+$");

	public SenchaRequestParser(@Inject(UPDATE_FILTER_LIST) List<UpdateFilter> updateFilters,
			@Inject IMetadataService metadataService, @Inject IJacksonService jacksonService,
			@Inject IRelationshipMapper associationHandler, @Inject ITreeProcessor treeProcessor,
			@Inject ISortProcessor sortProcessor, @Inject IFilterProcessor filterProcessor,
			@Inject IJsonValueConverterFactory jsonValueConverterFactory) {
		super(updateFilters, metadataService, jacksonService, associationHandler, treeProcessor, sortProcessor,
				filterProcessor, jsonValueConverterFactory);
	}

	@Override
	protected DataObjectProcessor createObjectProcessor(IJacksonService jacksonService,
			IRelationshipMapper associationHandler, IJsonValueConverterFactory jsonValueConverterFactory) {
		return new SenchaDataObjectProcessor(DASH_ID_PATTERN, jacksonService, associationHandler,
				jsonValueConverterFactory);
	}

}
