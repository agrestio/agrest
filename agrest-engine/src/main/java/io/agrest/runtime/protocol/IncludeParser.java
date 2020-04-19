package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.base.protocol.Include;
import io.agrest.runtime.entity.IncludeMerger;
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
    private ISizeParser sizeParser;

    public IncludeParser(
            @Inject IJacksonService jsonParser,
            @Inject ICayenneExpParser expParser,
            @Inject ISortParser sortParser,
            @Inject ISizeParser sizeParser) {

        this.jsonParser = jsonParser;
        this.expParser = expParser;
        this.sortParser = sortParser;
        this.sizeParser = sizeParser;
    }

    private static String getText(JsonNode node) {
        return node != null ? node.asText() : null;
    }

    @Override
    public List<Include> parse(String value) {

        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        List<Include> includes = new ArrayList<>();

        if (value.startsWith("[")) {
            appendFromArray(includes, jsonParser.parseJson(value), null);
        } else if (value.startsWith("{")) {
            appendFromObject(includes, jsonParser.parseJson(value), null);
        } else {
            appendPath(includes, value);
        }

        return includes;
    }

    private void appendFromArray(List<Include> includes, JsonNode node, String parentPath) {

        for (JsonNode child : node) {
            if (child.isObject()) {
                appendFromObject(includes, child, parentPath);
            } else if (child.isTextual()) {
                includes.add(new Include(parentPath != null ? parentPath + '.' + child.asText() : child.asText()));
            } else {
                throw new AgException(Response.Status.BAD_REQUEST, "Bad include spec: " + child);
            }
        }
    }

    private void appendFromObject(List<Include> includes, JsonNode node, String parentPath) {

        // nested include syntax: ___{"rel" : ["a1", "a2", "a3"]}___
        // TODO: We don't allow ordering, filtering, etc. in this case?
        if (node.size() == 1 && node.elements().next().isArray()) {

            String path = node.fieldNames().next();
            String absPath = parentPath != null ? parentPath + '.' + path : path;

            appendFromArray(includes, node.get(path), absPath);

        } else {

            String path = getText(node.get(JSON_KEY_PATH));
            String absPath = parentPath != null ? parentPath + '.' + path : path;

            includes.add(new Include(
                    absPath, expParser.fromJson(node.get(JSON_KEY_CAYENNE_EXP)),
                    sortParser.parseJson(node.get(JSON_KEY_SORT)),
                    getText(node.get(JSON_KEY_MAP_BY)),
                    sizeParser.startFromJson(node.get(JSON_KEY_START)),
                    sizeParser.limitFromJson(node.get(JSON_KEY_LIMIT))));

            JsonNode childIncludes = node.get(JSON_KEY_INCLUDE);

            if (childIncludes != null) {
                appendFromArray(includes, childIncludes, absPath);
            }
        }
    }

    private void appendPath(List<Include> includes, String path) {
        IncludeMerger.checkTooLong(path);

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        includes.add(new Include(path));
    }
}
