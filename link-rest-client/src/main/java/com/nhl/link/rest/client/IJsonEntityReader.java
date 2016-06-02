package com.nhl.link.rest.client;

import com.fasterxml.jackson.databind.JsonNode;

public interface IJsonEntityReader<T> {

    T readEntity(JsonNode node);
}
