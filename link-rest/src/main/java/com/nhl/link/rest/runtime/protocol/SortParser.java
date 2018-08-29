package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

public class SortParser implements ISortParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SortParser.class);

    private static final String JSON_KEY_PROPERTY = "property";
    private static final String JSON_KEY_DIRECTION = "direction";

    private IJacksonService jsonParser;

    public SortParser(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public Sort fromString(String sortValue) {
        if (sortValue == null || sortValue.isEmpty()) {
            return null;
        }

        if (sortValue.startsWith("[")) {
            List<Sort> sorts = fromJsonArray(jsonParser.parseJson(sortValue));
            return new Sort(sorts);
        } else if (sortValue.startsWith("{")) {
            return fromJsonObject(jsonParser.parseJson(sortValue));
        } else {
            return new Sort(sortValue);
        }
    }

    @Override
    public Sort fromJson(JsonNode json) {

        if (json == null || json.isNull()) {
            return null;
        }

        if (json.isTextual()) {
            return new Sort(json.asText());
        }

        if (json.isArray()) {
            List<Sort> sorts = fromJsonArray(json);
            return new Sort(sorts);
        }

        if (json.isObject()) {
            return fromJsonObject(json);
        }

        // TODO: throw?
        return null;
    }

    @Override
    public Dir dirFromString(String dirValue) {
        if (dirValue != null) {
            if (dirValue.equals(Dir.ASC.name())) {
                return Dir.ASC;
            } else if (dirValue.equals(Dir.DESC.name())) {
                return Dir.DESC;
            } else {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Direction is invalid: " + dirValue);
            }
        }
        return null;
    }

    private Sort fromJsonObject(JsonNode node) {

        if (node.isNull()) {
            return null;
        }

        JsonNode propertyNode = node.get(JSON_KEY_PROPERTY);
        if (propertyNode == null || !propertyNode.isTextual()) {

            // this is a hack for Sencha bug, passing us null sorters
            // per LF-189...
            // So allowing for lax property name checking as a result
            if (propertyNode != null && propertyNode.isNull()) {
                LOGGER.info("ignoring NULL sort property");
                return null;
            }

            throw new LinkRestException(Status.BAD_REQUEST, "Bad sort spec: " + node);
        }

        JsonNode directionNode = node.get(JSON_KEY_DIRECTION);
        if (directionNode != null) {
            Dir dir = dirFromString(directionNode.asText());
            if (dir != null) {
                return new Sort(propertyNode.asText(), dir);
            }
        }

        return new Sort(propertyNode.asText());
    }

    private List<Sort> fromJsonArray(JsonNode array) {

        List<Sort> sorts = new ArrayList<>(array.size());

        for (JsonNode sortNode : array) {
            Sort sort = fromJsonObject(sortNode);
            if (sort != null) {
                sorts.add(sort);
            }
        }

        return sorts;
    }
}
