package com.nhl.link.rest.runtime.parser.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.converter.UtcDateConverter;
import com.nhl.link.rest.runtime.parser.converter.ValueConverter;

class CayenneExpProcessor {

	private IJacksonService jsonParser;
	private Map<String, ValueConverter> converters;
	private IPathCache pathCache;

	CayenneExpProcessor(IJacksonService jsonParser, IPathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;

		this.converters = new HashMap<>();
		this.converters.put(Date.class.getName(), new UtcDateConverter());
	}

	void process(Entity<?> clientEntity, String cayenneExpJson) {
		if (cayenneExpJson == null || cayenneExpJson.length() == 0) {
			return;
		}

		JsonNode expNode = jsonParser.parseJson(cayenneExpJson);
		if (expNode != null) {
			process(clientEntity, expNode);
		}
	}

	void process(Entity<?> clientEntity, JsonNode expNode) {
		ObjEntity entity = clientEntity.getCayenneEntity();
		CayenneExpProcessorWorker worker = new CayenneExpProcessorWorker(expNode, converters, pathCache, entity);
		clientEntity.andQualifier(worker.exp());
	}
}
