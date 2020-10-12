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
        excludes.forEach(e -> processExcludePath(entity, e.getPath()));
    }

    private void processExcludePath(ResourceEntity<?> entity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> agEntity = entity.getAgEntity();

        if (dot < 0) {
            if (entity.removeAttribute(property) != null) {
                return;
            }
        }

        if (agEntity.getRelationship(property) != null) {

            // TODO: I guess we are not removing the relationships based on the assumption that they are not included
            //  by default and an exclude shouldn't be needed. But this is too much second-guessing the caller.

            ResourceEntity<?> child = entity.getChild(property);
            if (child == null) {
                // valid path, but not included... ignoring
                return;
            }

            if (dot > 0) {
                processExcludePath(child, path.substring(dot + 1));
            }

            return;
        }

        // this is an entity id and it's excluded explicitly
        if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            entity.excludeId();
            return;
        }

        // the property was either not included or is invalid... throw in the latter case for symmetry with "include"
        if (agEntity.getAttribute(property) == null
            // not checking relationship names; the condition above does it already...
        ) {
            throw new AgException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
        }
    }

    private void processOverlayExcludes(ResourceEntity<?> entity) {

        if (entity.getAgEntityOverlay() != null) {
            entity.getAgEntityOverlay().getExcludes().forEach(e -> exclude(entity, e));
        }

        entity.getChildren().values().forEach(this::processOverlayExcludes);
    }

    private void exclude(ResourceEntity<?> entity, String name) {
        if (entity.removeAttribute(name) == null
                && entity.removeChild(name) == null
                && name.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            entity.excludeId();
        }
    }
}
