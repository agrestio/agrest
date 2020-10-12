package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.base.protocol.Exclude;
import io.agrest.meta.AgEntity;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class ExcludeMerger implements IExcludeMerger {

    @Override
    public void merge(ResourceEntity<?> entity, List<Exclude> excludes) {
        processRequestExcludes(entity, excludes);
        processOverlayExcludes(entity);
    }

    private void processRequestExcludes(ResourceEntity<?> entity, List<Exclude> excludes) {
        excludes.forEach(e -> excludePath(entity, e.getPath()));
    }

    private void processOverlayExcludes(ResourceEntity<?> entity) {

        if (entity.getAgEntityOverlay() != null) {
            entity.getAgEntityOverlay().getExcludes().forEach(e -> exclude(entity, e));
        }

        entity.getChildren().values().forEach(this::processOverlayExcludes);
    }

    private void excludePath(ResourceEntity<?> entity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        if (dot >= 0) {
            excludeNonLeafPath(entity, path, dot);
        } else {
            excludeLeafPath(entity, path);
        }
    }

    private void excludeLeafPath(ResourceEntity<?> entity, String path) {

        boolean wasExcluded = exclude(entity, path);

        if (!wasExcluded && !isValidProperty(entity.getAgEntity(), path)) {
            // throw when the property is invalid (and not simply not included) for symmetry with "include"
            throw new AgException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
        }
    }

    private void excludeNonLeafPath(ResourceEntity<?> entity, String path, int dot) {
        String property = path.substring(0, dot);

        ResourceEntity<?> child = entity.getChild(property);
        // child may be null for a valid path that was not included...
        if (child != null) {
            excludePath(child, path.substring(dot + 1));
        }
    }

    private boolean exclude(ResourceEntity<?> entity, String name) {

        if (entity.removeAttribute(name) != null) {
            return true;
        }

        if (entity.removeChild(name) != null) {
            return true;
        }

        if (name.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            entity.excludeId();
            return true;
        }

        return false;
    }

    private boolean isValidProperty(AgEntity<?> entity, String name) {
        if (entity.getAttribute(name) != null) {
            return true;
        }

        if (entity.getRelationship(name) != null) {
            return true;
        }

        if (name.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            return true;
        }

        return false;
    }
}
