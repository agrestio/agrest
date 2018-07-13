package com.nhl.link.rest.runtime.parser.size;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.parser.converter.AbstractConverter;
import com.nhl.link.rest.runtime.query.Start;

import javax.ws.rs.core.Response;

public class StartConverter extends AbstractConverter<Start> {

    @Override
    protected Start valueNonNull(JsonNode node) {

        if (!node.isNumber()) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' value, got: " + node.asText());
        }

        return new Start(node.asInt());
    }

    public Start fromRootNode(JsonNode root) {
        JsonNode startNode = root.get(Start.getName());

        if (startNode != null) {
            return valueNonNull(startNode);
        }

        return null;
    }
}
