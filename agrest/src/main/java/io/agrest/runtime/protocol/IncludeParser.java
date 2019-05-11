package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.protocol.Include;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IncludeParser implements IIncludeParser {

    private static final String JSON_KEY_CAYENNE_EXP = "cayenneExp";
    private static final String JSON_KEY_PATH = "path";
    private static final String JSON_KEY_LIMIT = "limit";
    private static final String JSON_KEY_MAP_BY = "mapBy";
    private static final String JSON_KEY_SORT = "sort";
    private static final String JSON_KEY_START = "start";
    private static final String JSON_KEY_INCLUDE = "include";

    private IJacksonService jsonParser;
    private ICayenneExpParser expParser;
    private ISortParser sortParser;
    private IMapByParser mapByParser;
    private ISizeParser sizeParser;

    public IncludeParser(@Inject IJacksonService jsonParser,
                         @Inject ICayenneExpParser expParser,
                         @Inject ISortParser sortParser,
                         @Inject IMapByParser mapByParser,
                         @Inject ISizeParser sizeParser) {
        this.jsonParser = jsonParser;
        this.expParser = expParser;
        this.sortParser = sortParser;
        this.mapByParser = mapByParser;
        this.sizeParser = sizeParser;
    }

    @Override
    public List<Include> fromStrings(List<String> values) {
        List<Include> result = new ArrayList<>(values.size());

        for (String value : values) {
            result.add(oneFromString(value));
        }

        return result;
    }

    @Override
    public Include oneFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Include include;

        if (value.startsWith("[")) {
            List<Include> includes = fromArray(jsonParser.parseJson(value), null);
            include = new Include(includes);
        } else if (value.startsWith("{")) {
            include = fromJson(jsonParser.parseJson(value), null);
        } else {
            include = new Include(value);
        }

        return include;
    }

    private Include fromJson(JsonNode node, String parentPath) {

        // checks if JSON presents nested array
        if (node.size() == 1 && node.elements().next().isArray()) {
            List<Include> includes = fromArray(node.elements().next(), node.fieldNames().next());

            return new Include(includes);
        }

        JsonNode pathNode = node.get(JSON_KEY_PATH);
        String path = pathNode != null && pathNode.isTextual() ? pathNode.asText() : null;

        return new Include(
                expParser.fromJson(node.get(JSON_KEY_CAYENNE_EXP)),
                sortParser.fromJson(node.get(JSON_KEY_SORT)),
                mapByParser.fromJson(node.get(JSON_KEY_MAP_BY)),
                parentPath != null ? parentPath + '.' + path : path,
                sizeParser.startFromJson(node.get(JSON_KEY_START)),
                sizeParser.limitFromJson(node.get(JSON_KEY_LIMIT)),
                fromArray(node.get(JSON_KEY_INCLUDE), path));
    }

    private List<Include> fromArray(JsonNode node, String parentPath) {

        if(node == null) {
            return Collections.emptyList();
        }

        List<Include> includes = new ArrayList<>(node.size());

        for (JsonNode child : node) {
            if (child.isObject()) {
                includes.add(fromJson(child, parentPath));
            } else if (child.isTextual()) {
                includes.add(new Include(parentPath != null ? parentPath + '.' + child.asText() : child.asText()));
            } else {
                throw new AgException(Response.Status.BAD_REQUEST, "Bad include spec: " + child);
            }
        }

        return includes;
    }


}
