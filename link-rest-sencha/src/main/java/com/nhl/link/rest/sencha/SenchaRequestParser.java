package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
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

	public SenchaRequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor,
            @Inject ISenchaFilterProcessor senchaFilterProcessor) {

		super(includeProcessor, excludeProcessor, sortProcessor, cayenneExpProcessor);

		this.senchaFilterProcessor = senchaFilterProcessor;
	}

	@Override
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {

		ResourceEntity<T> resourceEntity = super.parseSelect(entity, protocolParameters);

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
