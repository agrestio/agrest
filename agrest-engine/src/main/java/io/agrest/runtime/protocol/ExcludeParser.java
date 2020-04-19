package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.base.protocol.Exclude;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ExcludeParser implements IExcludeParser {

    private IJacksonService jsonParser;

    public ExcludeParser(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public List<Exclude> parse(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        List<Exclude> excludes = new ArrayList<>();
        if (value.startsWith("[")) {
            appendFromArray(excludes, jsonParser.parseJson(value), null);
        } else {
            appendPath(excludes, value);
        }

        return excludes;
    }

    private void appendFromArray(List<Exclude> excludes, JsonNode node, String parentPath) {

        for (JsonNode child : node) {

            if (child.isObject()) {

                // nested exclude syntax: ___"rel" : ["e1", "e2", "e3"]___
                if (child.size() == 1 && child.elements().next().isArray()) {
                    String field = child.fieldNames().next();
                    // TODO: prepend parentPath to field name
                    appendFromArray(excludes, child.get(field), field);
                }

            } else if (child.isTextual()) {
                appendPath(excludes, parentPath != null ? parentPath + '.' + child.asText() : child.asText());
            } else {
                throw new AgException(Response.Status.BAD_REQUEST, "Bad exclude spec: " + child);
            }
        }
    }

    private void appendPath(List<Exclude> excludes, String path) {
        IncludeMerger.checkTooLong(path);

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        excludes.add(new Exclude(path));
    }
}
