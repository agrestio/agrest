package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.QueryParamConverter;
import com.nhl.link.rest.runtime.parser.mapBy.MapByConverter;
import com.nhl.link.rest.runtime.parser.size.LimitConverter;
import com.nhl.link.rest.runtime.parser.size.StartConverter;
import com.nhl.link.rest.runtime.parser.sort.SortConverter;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpConverter;
import com.nhl.link.rest.runtime.query.Include;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class IncludeConverter extends QueryParamConverter<Include> {

    private static final String PATH = "path";

    private IJacksonService jsonParser;
    private CayenneExpConverter expConverter;
    private SortConverter sortConverter;
    private MapByConverter mapByConverter;
    private StartConverter startConverter;
    private LimitConverter limitConverter;

    public IncludeConverter(IJacksonService jsonParser,
                            CayenneExpConverter expConverter,
                            SortConverter sortConverter,
                            MapByConverter mapByConverter,
                            StartConverter startConverter,
                            LimitConverter limitConverter) {
        this.jsonParser = jsonParser;
        this.expConverter = expConverter;
        this.sortConverter = sortConverter;
        this.mapByConverter = mapByConverter;
        this.startConverter = startConverter;
        this.limitConverter = limitConverter;
    }

    @Override
    public Include fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Include include;

        if (value.startsWith("[")) {
            List<Include> includes = fromArray(jsonParser.parseJson(value));
            include = new Include(includes);
        } else if (value.startsWith("{")) {
            include = value(jsonParser.parseJson(value));
        } else {
            include = new Include(value);
        }

        return include;
    }

    @Override
    public Include fromRootNode(JsonNode root) {
        return null;
    }

    @Override
    protected Include valueNonNull(JsonNode node) {
        if (node == null) {
            return null;
        }

        JsonNode pathNode = node.get(PATH);

        return new Include( expConverter.fromRootNode(node),
                            sortConverter.fromRootNode(node),
                            mapByConverter.fromRootNode(node),
                            pathNode != null && pathNode.isTextual() ? pathNode.asText() : null,
                            startConverter.fromRootNode(node),
                            limitConverter.fromRootNode(node));
    }

    private List<Include> fromArray(JsonNode root) {
        List<Include> includes = new ArrayList<>();

        if (root != null && root.isArray()) {
            for (JsonNode child : root) {
                Include include;
                if (child.isObject()) {
                    include = value(child);
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
