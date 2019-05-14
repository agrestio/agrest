package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.protocol.Exclude;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.jackson.IJacksonService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class ExcludeParser implements IExcludeParser {

    private IJacksonService jsonParser;

    public ExcludeParser(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public Exclude parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Exclude exclude;

        if (value.startsWith("[")) {
            List<Exclude> excludes = fromArray(jsonParser.parseJson(value), null);
            exclude = new Exclude(excludes);
        } else {
            exclude = getExcludeObject(value);
        }

        return exclude;
    }

    private List<Exclude> fromArray(JsonNode root, String parentPath) {
        List<Exclude> excludes = new ArrayList<>();

        if (root != null && root.isArray()) {

            for (JsonNode child : root) {
                Exclude exclude = null;
                if (child.isObject()) {
                    // checks if JSON presents nested array
                    if (child.size() == 1 && child.elements().next().isArray()) {
                        exclude = new Exclude(fromArray(child.elements().next(), child.fieldNames().next()));
                    }
                } else if (child.isTextual()) {
                    exclude = getExcludeObject(parentPath != null ? parentPath + '.' + child.asText() : child.asText());
                } else {
                    throw new AgException(Response.Status.BAD_REQUEST, "Bad exclude spec: " + child);
                }

                if (exclude != null) {
                    excludes.add(exclude);
                }
            }
        }

        return excludes;
    }

    private Exclude getExcludeObject(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        IncludeMerger.checkTooLong(value);

        int dot = value.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude starts with dot: " + value);
        }

        if (dot == value.length() - 1) {
            throw new AgException(Response.Status.BAD_REQUEST, "Exclude ends with dot: " + value);
        }

        return new Exclude(value);
    }
}
