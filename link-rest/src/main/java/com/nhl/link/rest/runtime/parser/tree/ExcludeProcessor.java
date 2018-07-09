package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.runtime.query.Exclude;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ParamConverter;
import java.util.List;

public class ExcludeProcessor implements IExcludeProcessor {

    private ExcludeConverter converter;

    public ExcludeProcessor(@Inject IJacksonService jsonParser) {
        this.converter = new ExcludeConverter(jsonParser);
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, List<String> values) {
        for (String value : values) {
            Exclude exclude = converter.fromString(value);
            processOne(resourceEntity, exclude);
        }
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, Query query) {
        for (Exclude exclude : query.getExclude()) {
            processOne(resourceEntity, exclude);
        }
    }

    @Override
    public ParamConverter<?> getConverter() {
        return converter;
    }

    private void processOne(ResourceEntity<?> resourceEntity, Exclude exclude) {
        processExcludePath(resourceEntity, exclude.getPath());
        // processes nested includes
        if (exclude != null) {
            exclude.getExcludes().stream().forEach(e -> processExcludePath(resourceEntity, e.getPath()));
        }
    }

    private void processExcludePath(ResourceEntity<?> resourceEntity, String path) {
        if (path == null) {
            return;
        }

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new LinkRestException(Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new LinkRestException(Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        if (resourceEntity.getLrEntity().getAttribute(property) != null) {

            if (dot > 0) {
                throw new LinkRestException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
            }

            resourceEntity.getAttributes().remove(property);
            return;
        }

        if (resourceEntity.getLrEntity().getRelationship(property) != null) {

            ResourceEntity<?> relatedFilter = resourceEntity.getChild(property);
            if (relatedFilter == null) {
                // valid path, but not included... ignoring
                return;
            }

            if (dot > 0) {
                processExcludePath(relatedFilter, path.substring(dot + 1));
            }
            return;
        }

        // this is an entity id and it's excluded explicitly
        if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            resourceEntity.excludeId();
            return;
        }

        throw new LinkRestException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
    }
}
