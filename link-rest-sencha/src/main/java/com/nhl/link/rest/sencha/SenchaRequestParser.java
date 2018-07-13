package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByProcessor;
import com.nhl.link.rest.runtime.parser.size.ISizeProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Exclude;
import com.nhl.link.rest.runtime.query.Include;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.MapBy;
import com.nhl.link.rest.runtime.query.Sort;
import com.nhl.link.rest.runtime.query.Start;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

/**
 * @since 1.11
 */
public class SenchaRequestParser extends RequestParser {

	static final String FILTER = "filter";
	static final String GROUP = "group";
	static final String GROUP_DIR = "groupDir";

	private ISenchaFilterProcessor senchaFilterProcessor;
	private IIncludeProcessor includeProcessor;
	private IExcludeProcessor excludeProcessor;
	private ISortProcessor sortProcessor;
	private ICayenneExpProcessor cayenneExpProcessor;
	private IMapByProcessor mapByProcessor;
	private ISizeProcessor sizeProcessor;


	public SenchaRequestParser(
            @Inject IIncludeProcessor includeProcessor,
            @Inject IExcludeProcessor excludeProcessor,
            @Inject ISortProcessor sortProcessor,
            @Inject ICayenneExpProcessor cayenneExpProcessor,
			@Inject IMapByProcessor mapByProcessor,
            @Inject ISizeProcessor sizeProcessor,
            @Inject ISenchaFilterProcessor senchaFilterProcessor) {

		super(includeProcessor, excludeProcessor, sortProcessor, cayenneExpProcessor, mapByProcessor, sizeProcessor);

		this.senchaFilterProcessor = senchaFilterProcessor;
		this.includeProcessor = includeProcessor;
		this.excludeProcessor = excludeProcessor;
		this.sortProcessor = sortProcessor;
		this.cayenneExpProcessor = cayenneExpProcessor;
		this.mapByProcessor = mapByProcessor;
		this.sizeProcessor = sizeProcessor;
	}

	@Override
	public <T> ResourceEntity<T> parseSelect(LrEntity<T> entity, Map<String, List<String>> protocolParameters) {
		ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

		sizeProcessor.process(resourceEntity, BaseRequestProcessor.integer(protocolParameters, Start.getName()),
				BaseRequestProcessor.integer(protocolParameters, Limit.getName()));

		includeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Include.getName()));
		excludeProcessor.process(resourceEntity, BaseRequestProcessor.strings(protocolParameters, Exclude.getName()));

		sortProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, GROUP),
				BaseRequestProcessor.string(protocolParameters, GROUP_DIR));
		sortProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, Sort.getName()),
				BaseRequestProcessor.string(protocolParameters, "dir"));

		mapByProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, MapBy.getName()));
		cayenneExpProcessor.process(resourceEntity, BaseRequestProcessor.string(protocolParameters, CayenneExp.getName()));


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
