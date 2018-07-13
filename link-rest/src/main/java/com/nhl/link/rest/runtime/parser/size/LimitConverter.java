package com.nhl.link.rest.runtime.parser.size;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.parser.converter.AbstractConverter;
import com.nhl.link.rest.runtime.query.Limit;

import javax.ws.rs.core.Response;

public class LimitConverter extends AbstractConverter<Limit> {

    @Override
    protected Limit valueNonNull(JsonNode node) {

        if (!node.isNumber()) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' value, got: " + node.asText());
        }

        return new Limit(node.asInt());
    }

    public Limit fromRootNode(JsonNode root) {
        JsonNode limitNode = root.get(Limit.getName());

        if (limitNode != null) {
            return valueNonNull(limitNode);
        }

        return null;
    }
}
