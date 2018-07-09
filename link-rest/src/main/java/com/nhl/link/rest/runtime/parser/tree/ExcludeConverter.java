package com.nhl.link.rest.runtime.parser.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.runtime.query.Exclude;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import java.util.ArrayList;
import java.util.List;

public class ExcludeConverter implements ParamConverter<Exclude> {

    private IJacksonService jsonParser;

    public ExcludeConverter(IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public Exclude fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Exclude exclude = new Exclude();

        if (value.startsWith("[")) {
            List<Exclude> excludes = fromArray(value);
            exclude.setExcludes(excludes);
        } else {
            exclude = getExcludeObject(value);
        }

        return exclude;
    }

    @Override
    public String toString(Exclude value) {
        return null;
    }

    private List<Exclude> fromArray(String value) {
        List<Exclude> excludes = new ArrayList<>();

        JsonNode root = jsonParser.parseJson(value);

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

        BaseRequestProcessor.checkTooLong(value);

        Exclude exclude = new Exclude();

        int dot = value.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Exclude starts with dot: " + value);
        }

        if (dot == value.length() - 1) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Exclude ends with dot: " + value);
        }

        exclude.setPath(value);

        return exclude;
    }
}
