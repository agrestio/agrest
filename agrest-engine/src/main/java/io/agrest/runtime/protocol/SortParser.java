package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.access.PathChecker;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortParser implements ISortParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SortParser.class);

    /**
     * @since protocol v1.2, Agrest 5.0
     */
    private static final String JSON_KEY_PATH = "path";

    /**
     * @deprecated since protocol v1.2, Agrest 5.0
     */
    @Deprecated
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

    private Direction parseDirection(String unparsed) {
        try {
            // "direction" is case-insensitive in the protocol since 1.2
            return Direction.valueOf(unparsed.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw AgException.badRequest("'direction' is invalid: %s", unparsed);
        }
    }

    private void appendFromArray(List<Sort> orderings, JsonNode node) {
        for (JsonNode sortNode : node) {
            appendFromObject(orderings, sortNode);
        }
    }

    private void appendFromObject(List<Sort> orderings, JsonNode node) {

        JsonNode pathNode = pathNode(node);
        if (pathNode == null || !pathNode.isTextual()) {
            throw AgException.badRequest("Bad sort spec: %s", node);
        }

        JsonNode directionNode = node.get(JSON_KEY_DIRECTION);
        appendPath(
                orderings,
                pathNode.asText(),
                directionNode != null ? directionNode.asText() : null);
    }

    private void appendPath(List<Sort> orderings, String path, String unparsedDirection) {
        PathChecker.exceedsLength(path);

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw AgException.badRequest("Ordering starts with dot: %s", path);
        }

        if (dot == path.length() - 1) {
            throw AgException.badRequest("Ordering ends with dot: %s", path);
        }

        Direction direction = unparsedDirection != null && !unparsedDirection.isEmpty()
                ? parseDirection(unparsedDirection)
                : Direction.asc;
        orderings.add(new Sort(path, direction));
    }

    private JsonNode pathNode(JsonNode sortNode) {
        JsonNode pathNode = sortNode.get(JSON_KEY_PATH);
        if (pathNode != null) {
            return pathNode;
        }

        JsonNode propertyNode = sortNode.get(JSON_KEY_PROPERTY);
        if (propertyNode != null) {
            LOGGER.info(
                    "*** '{}' property of the 'sort' object is deprecated in protocol v1.2 (Agrest 5.0). Consider replacing it with '{}'",
                    JSON_KEY_PROPERTY,
                    JSON_KEY_PATH);
            return propertyNode;
        }

        return null;
    }
}
