package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IMapByProcessor;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

import static com.nhl.link.rest.Term.DIR;
import static com.nhl.link.rest.Term.SORT;

/**
 * @since 1.11
 */
public class SenchaRequestParser extends RequestParser {

	static final String FILTER = "filter";

	private ISenchaFilterProcessor senchaFilterProcessor;
	private ISortProcessor sortProcessor;



	public SenchaRequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor,
			@Inject IMapByProcessor mapByProcessor,
            @Inject ISenchaFilterProcessor senchaFilterProcessor) {

		super(includeProcessor, excludeProcessor, sortProcessor, cayenneExpProcessor, mapByProcessor);

		this.sortProcessor = sortProcessor;
		this.senchaFilterProcessor = senchaFilterProcessor;
	}

	@Override
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {

		SORT.setValue("group");
		DIR.setValue("groupDir");

		ResourceEntity<T> resourceEntity = super.parseSelect(entity, protocolParameters);

		SORT.setValue("sort");
		DIR.setValue("dir");

		sortProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, SORT),
											BaseRequestProcessor.string(protocolParameters, DIR));

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
