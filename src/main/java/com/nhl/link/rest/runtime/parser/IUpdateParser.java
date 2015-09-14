package com.nhl.link.rest.runtime.parser;

import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.20
 */
public interface IUpdateParser {

	<T> Collection<EntityUpdate<T>> parse(LrEntity<T> entityType, JsonNode json);
}
