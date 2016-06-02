package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 2.0
 */
public interface IJsonEntityReader<T> {

    T readEntity(JsonNode node);
}
