package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.entity.IncludeMerger;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.PathConstants;
import com.nhl.link.rest.protocol.Exclude;
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
    public List<Exclude> fromStrings(List<String> values) {
        List<Exclude> result = new ArrayList<>();

        for (String value : values) {
            Exclude exclude = oneFromString(value);
            if (exclude != null) {
                result.add(exclude);
            }
        }

        return !result.isEmpty() ? result : null;
    }

    private Exclude oneFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Exclude exclude;

        if (value.startsWith("[")) {
            List<Exclude> excludes = fromArray(jsonParser.parseJson(value));
            exclude = new Exclude(excludes);
        } else {
            exclude = getExcludeObject(value);
        }

        return exclude;
    }

    private List<Exclude> fromArray(JsonNode root) {
        List<Exclude> excludes = new ArrayList<>();

        if (root != null && root.isArray()) {

            for (JsonNode child : root) {
                if (child.isTextual()) {
                    Exclude exclude = getExcludeObject(child.asText());
                    if (exclude != null) {
                        excludes.add(exclude);
                    }
                } else {
                    throw new LinkRestException(Response.Status.BAD_REQUEST, "Bad exclude spec: " + child);
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
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Exclude starts with dot: " + value);
        }

        if (dot == value.length() - 1) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Exclude ends with dot: " + value);
        }

        return new Exclude(value);
    }
}
