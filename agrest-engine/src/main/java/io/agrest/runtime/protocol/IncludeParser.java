package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.base.protocol.AgProtocol;
import io.agrest.base.protocol.Include;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IncludeParser implements IIncludeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeParser.class);

    // keys in "include" JSON overlap with the main protocol, but not entirely
    private static final String JSON_KEY_PATH = "path";

    /**
     * @since 4.1
     */
    private static final String INCLUDE_EXP = AgProtocol.exp.name();

    /**
     * @deprecated since 4.1 in favor of {@link AgProtocol#exp}, but will be supported indefinitely for backwards
     * compatibility.
     */
    @Deprecated
    private static final String INCLUDE_CAYENNE_EXP = AgProtocol.cayenneExp.name();
    private static final String INCLUDE_LIMIT = AgProtocol.limit.name();
    private static final String INCLUDE_MAP_BY = AgProtocol.mapBy.name();
    private static final String INCLUDE_SORT = AgProtocol.sort.name();
    private static final String INCLUDE_START = AgProtocol.start.name();
    private static final String INCLUDE_INCLUDE = AgProtocol.include.name();

    private final IJacksonService jsonParser;
    private final IExpParser expParser;
    private final ISortParser sortParser;
    private final ISizeParser sizeParser;

    public IncludeParser(
            @Inject IJacksonService jsonParser,
            @Inject IExpParser expParser,
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
                throw AgException.badRequest("Bad include spec: %s", child);
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
                    absPath,
                    expParser.fromJson(getExp(node)),
                    sortParser.parseJson(node.get(INCLUDE_SORT)),
                    getText(node.get(INCLUDE_MAP_BY)),
                    sizeParser.startFromJson(node.get(INCLUDE_START)),
                    sizeParser.limitFromJson(node.get(INCLUDE_LIMIT))));

            JsonNode childIncludes = node.get(INCLUDE_INCLUDE);

            if (childIncludes != null) {
                appendFromArray(includes, childIncludes, absPath);
            }
        }
    }

    private JsonNode getExp(JsonNode include) {

        JsonNode exp = include.get(INCLUDE_EXP);
        JsonNode cayenneExp = include.get(INCLUDE_CAYENNE_EXP);

        if (exp != null) {
            return exp;
        }

        // keep supporting deprecated "cayenneExp" key
        if (cayenneExp != null) {
            LOGGER.info("*** 'cayenneExp' include parameter is deprecated since Agrest 4.1. Consider replacing it with 'exp'");
        }

        return cayenneExp;
    }

    private void appendPath(List<Include> includes, String path) {
        IncludeMerger.checkTooLong(path);

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw AgException.badRequest("Exclude starts with dot: %s", path);
        }

        if (dot == path.length() - 1) {
            throw AgException.badRequest("Exclude ends with dot: %s", path);
        }

        includes.add(new Include(path));
    }
}
