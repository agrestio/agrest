package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.protocol.Include;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class IncludeParser implements IIncludeParser {

    private static final String JSON_KEY_CAYENNE_EXP = "cayenneExp";
    private static final String JSON_KEY_PATH = "path";
    private static final String JSON_KEY_LIMIT = "limit";
    private static final String JSON_KEY_MAP_BY = "mapBy";
    private static final String JSON_KEY_SORT = "sort";
    private static final String JSON_KEY_START = "start";

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
        List<Include> result = new ArrayList<>();

        for (String value : values) {
            Include include = oneFromString(value);
            if (include != null) {
                result.add(include);
            }
        }

        return !result.isEmpty() ? result : null;
    }

    @Override
    public Include oneFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Include include;

        if (value.startsWith("[")) {
            List<Include> includes = fromArray(jsonParser.parseJson(value));
            include = new Include(includes);
        } else if (value.startsWith("{")) {
            include = fromJson(jsonParser.parseJson(value));
        } else {
            include = new Include(value);
        }

        return include;
    }

    private Include fromJson(JsonNode node) {
        if (node == null) {
            return null;
        }

        JsonNode pathNode = node.get(JSON_KEY_PATH);
        String path = pathNode != null && pathNode.isTextual() ? pathNode.asText() : null;

        return new Include(expParser.fromJson(node.get(JSON_KEY_CAYENNE_EXP)),
                sortParser.fromJson(node.get(JSON_KEY_SORT)),
                mapByParser.fromJson(node.get(JSON_KEY_MAP_BY)),
                path,
                sizeParser.startFromJson(node.get(JSON_KEY_START)),
                sizeParser.limitFromJson(node.get(JSON_KEY_LIMIT)));
    }

    private List<Include> fromArray(JsonNode root) {
        List<Include> includes = new ArrayList<>();

        if (root != null && root.isArray()) {
            for (JsonNode child : root) {
                Include include;
                if (child.isObject()) {
                    include = fromJson(child);
                } else if (child.isTextual()) {
                    include = new Include(child.asText());
                } else {
                    throw new AgException(Response.Status.BAD_REQUEST, "Bad include spec: " + child);
                }

                if (include != null) {
                    includes.add(include);
                }
            }
        }

        return includes;
    }
}
