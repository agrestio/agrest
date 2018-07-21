package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.PathConstants;
import com.nhl.link.rest.protocol.Exclude;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class ExcludeConstructor implements IExcludeConstructor {

    @Override
    public void construct(ResourceEntity<?> resourceEntity, List<Exclude> excludes) {
        for (Exclude exclude : excludes) {
            processOne(resourceEntity, exclude);
        }
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
