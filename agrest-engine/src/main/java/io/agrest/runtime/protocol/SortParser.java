package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.base.protocol.Dir;
import io.agrest.base.protocol.Sort;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
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
    public List<Sort> parse(String unparsedSort, String unparsedDir) {

        if (unparsedSort == null || unparsedSort.isEmpty()) {
            return Collections.emptyList();
        }

        List<Sort> orderings = new ArrayList<>(3);

        if (unparsedSort.startsWith("[")) {
            appendFromArray(orderings, jsonParser.parseJson(unparsedSort));
        } else if (unparsedSort.startsWith("{")) {
            appendFromObject(orderings, jsonParser.parseJson(unparsedSort));
        } else {
            // "dir" is only applicable to "simple" sorts.. ignoring it for arrays and objects
            appendPath(orderings, unparsedSort, unparsedDir);
        }

        return orderings;
    }

    @Override
    public List<Sort> parseJson(JsonNode json) {

        if (json == null || json.isNull()) {
            return Collections.emptyList();
        }

        List<Sort> orderings = new ArrayList<>(3);

        if (json.isArray()) {
            appendFromArray(orderings, json);
        } else if (json.isObject()) {
            appendFromObject(orderings, json);
        } else {
            appendPath(orderings, json.asText(), null);
        }

        return orderings;
    }

    private Dir parseDir(String unparsedDir) {
        try {
            return Dir.valueOf(unparsedDir);
        } catch (IllegalArgumentException e) {
            throw new AgException(Response.Status.BAD_REQUEST, "'dir' is invalid: " + unparsedDir);
        }
    }

    private void appendFromArray(List<Sort> orderings, JsonNode node) {
        for (JsonNode sortNode : node) {
            appendFromObject(orderings, sortNode);
        }
    }

    private void appendFromObject(List<Sort> orderings, JsonNode node) {

        JsonNode propertyNode = node.get(JSON_KEY_PROPERTY);
        if (propertyNode == null || !propertyNode.isTextual()) {

            // this is a hack for Sencha bug, passing us null sorters
            // per LF-189... So allowing for lax property name checking as a result
            // TODO: move this to Sencha package?
            if (propertyNode != null && propertyNode.isNull()) {
                LOGGER.info("ignoring NULL sort property");
                return;
            }

            throw new AgException(Status.BAD_REQUEST, "Bad sort spec: " + node);
        }

        JsonNode directionNode = node.get(JSON_KEY_DIRECTION);
        appendPath(
                orderings,
                propertyNode.asText(),
                directionNode != null ? directionNode.asText() : null);
    }

    private void appendPath(List<Sort> orderings, String path, String unparsedDir) {
        IncludeMerger.checkTooLong(path);

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Response.Status.BAD_REQUEST, "Ordering starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Response.Status.BAD_REQUEST, "Ordering ends with dot: " + path);
        }

        Dir dir = unparsedDir != null && !unparsedDir.isEmpty() ? parseDir(unparsedDir) : Dir.ASC;
        orderings.add(new Sort(path, dir));
    }
}
