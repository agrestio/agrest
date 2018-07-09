package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Include;
import com.nhl.link.rest.runtime.query.Sort;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import java.util.ArrayList;
import java.util.List;

import static com.nhl.link.rest.Term.MAP_BY;
import static com.nhl.link.rest.Term.SORT;
import static com.nhl.link.rest.Term.CAYENNE_EXP;
import static com.nhl.link.rest.Term.START;
import static com.nhl.link.rest.Term.LIMIT;

public class IncludeConverter implements ParamConverter<Include> {

    private static final String PATH = "path";

    private IJacksonService jsonParser;
    private ICayenneExpProcessor expProcessor;
    private ISortProcessor sortProcessor;

    public IncludeConverter(IJacksonService jsonParser, ICayenneExpProcessor expProcessor, ISortProcessor sortProcessor) {
        this.jsonParser = jsonParser;
        this.expProcessor = expProcessor;
        this.sortProcessor = sortProcessor;
    }

    @Override
    public Include fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Include include = new Include();

        if (value.startsWith("[")) {
            List<Include> includes = fromArray(jsonParser.parseJson(value));
            include.setIncludes(includes);
        } else if (value.startsWith("{")) {
            include = getIncludeObject(jsonParser.parseJson(value));
        } else {
            include.setValue(value);
        }

        return include;
    }

    @Override
    public String toString(Include value) {
        return null;
    }

    private List<Include> fromArray(JsonNode root) {
        List<Include> includes = new ArrayList<>();

        if (root != null && root.isArray()) {
            for (JsonNode child : root) {
                Include include = new Include();
                if (child.isObject()) {
                    include = getIncludeObject(child);
                } else if (child.isTextual()) {
                    include.setValue(child.asText());
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

    private Include getIncludeObject(JsonNode root) {

        if (root == null) {
            return null;
        }

        Include include = new Include();

        JsonNode pathNode = root.get(PATH);
        if (pathNode != null && pathNode.isTextual()) {
            include.setPath(pathNode.asText());
        }

        JsonNode mapByNode = root.get(MAP_BY.toString());
        if (mapByNode != null) {
            if (!mapByNode.isTextual()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Bad include spec - invalid 'mapBy': " + root);
            }

            include.setMapBy(mapByNode.asText());
        }

        JsonNode sortNode = root.get(SORT.toString());
        if (sortNode != null) {
            Sort sort = (Sort) sortProcessor.getConverter().fromString(sortNode.isTextual() ? sortNode.asText() : sortNode.toString());
            include.setSort(sort);
        }

        JsonNode expNode = root.get(CAYENNE_EXP.toString());
        if (expNode != null) {
            CayenneExp cayenneExp = expProcessor.getConverter().fromString(expNode.isTextual() ? expNode.asText() : expNode.toString());
            include.setCayenneExp(cayenneExp);
        }

        JsonNode startNode = root.get(START.toString());
        if (startNode != null) {
            include.setStart(startNode.asInt());
        }

        JsonNode limitNode = root.get(LIMIT.toString());
        if (limitNode != null) {
            include.setLimit(limitNode.asInt());
        }

        return include;
    }

}
