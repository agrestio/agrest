package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.size.ISizeParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.protocol.Include;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class IncludeParser implements IIncludeParser {

    private static final String PATH = "path";

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

    private Include oneFromString(String value) {
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

        JsonNode pathNode = node.get(PATH);

        return new Include( expParser.fromRootNode(node),
                            sortParser.fromRootNode(node),
                            mapByParser.fromRootNode(node),
                            pathNode != null && pathNode.isTextual() ? pathNode.asText() : null,
                            sizeParser.startFromRootNode(node),
                            sizeParser.limitFromRootNode(node));
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
                    throw new LinkRestException(Response.Status.BAD_REQUEST, "Bad include spec: " + child);
                }

                if (include != null) {
                    includes.add(include);
                }
            }
        }

        return includes;
    }
}
