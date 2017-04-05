package com.nhl.link.rest.runtime.adapter.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

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
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters, String autocompleteProperty) {

		ResourceEntity<T> resourceEntity = super.parseSelect(entity, protocolParameters, autocompleteProperty);

		Expression e1 = parseFilter(entity, protocolParameters);
		if (e1 != null) {
			resourceEntity.andQualifier(e1);
		}

		return resourceEntity;
	}

	protected Expression parseFilter(LrEntity<?> entity, Map<String, List<String>> protocolParameters) {
		String value = BaseRequestProcessor.string(protocolParameters, FILTER);
		return senchaFilterProcessor.process(entity, value);
	}

}
