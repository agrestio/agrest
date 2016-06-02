package com.nhl.link.rest.client;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 2.0
 */
public interface IJsonEntityReader<T> {

    T readEntity(JsonNode node);
}
