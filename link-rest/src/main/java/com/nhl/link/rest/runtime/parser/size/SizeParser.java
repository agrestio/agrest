package com.nhl.link.rest.runtime.parser.size;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.Start;

import javax.ws.rs.core.Response;

public class SizeParser implements ISizeParser{

    @Override
    public Start startFromRootNode(JsonNode root) {
        JsonNode startNode = root.get(Start.START);

        if (startNode != null) {
            if (!startNode.isNumber()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' as 'start' value, got: " + startNode.asText());
            }

            return new Start(startNode.asInt());
        }

        return null;
    }

    @Override
    public Limit limitFromRootNode(JsonNode root) {
        JsonNode limitNode = root.get(Limit.LIMIT);

        if (limitNode != null) {
            if (!limitNode.isNumber()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' as 'limit' value, got: " + limitNode.asText());
            }

            return new Limit(limitNode.asInt());
        }

        return null;
    }


}
