package com.nhl.link.rest.runtime.parser.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.UtcDateConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;

public class CayenneExpProcessor implements ICayenneExpProcessor {

	private IJacksonService jsonParser;
	private Map<Class<?>, JsonValueConverter> converters;
	private IPathCache pathCache;

	public CayenneExpProcessor(@Inject IJacksonService jsonParser, @Inject IPathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;

		// TODO: instead of manually assembling converters we must switch to
		// IJsonValueConverterFactory already used by DataObjectProcessor.
		// The tricky part is the "id" attribute that is converted to DbPath
		// during CayenneExpProcessorWorker traversal, so its type can not be
		// mapped with existing tools

		this.converters = new HashMap<>();
		this.converters.put(Date.class, UtcDateConverter.converter());
		this.converters.put(java.sql.Date.class, UtcDateConverter.converter());
		this.converters.put(java.sql.Time.class, UtcDateConverter.converter());
		this.converters.put(java.sql.Timestamp.class, UtcDateConverter.converter());
	}

	@Override
	public Expression process(LrEntity<?> entity, String expressionString) {

		if (expressionString == null || expressionString.length() == 0) {
			return null;
		}

		return worker(entity).exp(expressionString);
	}

	@Override
	public Expression process(LrEntity<?> entity, JsonNode expressionNode) {

		if (expressionNode == null) {
			return null;
		}

		return worker(entity).exp(expressionNode);
	}

	private CayenneExpProcessorWorker worker(LrEntity<?> entity) {
		return new CayenneExpProcessorWorker(jsonParser, converters, pathCache, entity);
	}
}
