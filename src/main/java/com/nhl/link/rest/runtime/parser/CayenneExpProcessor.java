package com.nhl.link.rest.runtime.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.runtime.parser.converter.UtcDateConverter;
import com.nhl.link.rest.runtime.parser.converter.ValueConverter;

class CayenneExpProcessor {

	private RequestJsonParser jsonParser;
	private Map<String, ValueConverter> converters;
	private PathCache pathCache;

	CayenneExpProcessor(RequestJsonParser jsonParser, PathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;

		this.converters = new HashMap<>();
		this.converters.put(Date.class.getName(), new UtcDateConverter());
	}

	void process(Entity<?> clientEntity, String cayenneExpJson) {
		if (cayenneExpJson == null || cayenneExpJson.length() == 0) {
			return;
		}

		JsonNode expNode = jsonParser.parseJSON(cayenneExpJson, new ObjectMapper());
		if (expNode != null) {
			process(clientEntity, expNode);
		}
	}

	void process(Entity<?> clientEntity, JsonNode expNode) {
		ObjEntity entity = clientEntity.getEntity();

		EntityPathCache entityPathCache = pathCache.entityPathCache(entity);

		CayenneExpProcessorWorker worker = new CayenneExpProcessorWorker(expNode, converters, entityPathCache);
		clientEntity.andQualifier(worker.exp());
	}
}
