package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.base.protocol.Exclude;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class ExcludeMerger implements IExcludeMerger {

    @Override
    public void merge(ResourceEntity<?> resourceEntity, List<Exclude> excludes) {
        for (Exclude exclude : excludes) {
            processExcludePath(resourceEntity, exclude.getPath());
        }
    }

    private void processExcludePath(ResourceEntity<?> resourceEntity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Status.BAD_REQUEST, "Exclude starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Status.BAD_REQUEST, "Exclude ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> entity = resourceEntity.getAgEntity();

        if (dot < 0) {

            if (resourceEntity.getIncludedExtraProperties().remove(property) != null) {
                return;
            }

            if (resourceEntity.getAttributes().remove(property) != null) {
                return;
            }
        }

        if (entity.getRelationship(property) != null) {

            // TODO: I guess we are not removing the relationships based on the assumption that they are not included
            //  by default and an exclude shouldn't be needed. But this is too much second-guessing the caller.

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

        // the property was either not included or is invalid... throw in the latter case for symmetry with "include"
        if (entity.getAttribute(property) == null
                && !resourceEntity.getExtraProperties().containsKey(property)
            // not checking relationship names; the condition above does it already...
        ) {
            throw new AgException(Status.BAD_REQUEST, "Invalid exclude path: " + path);
        }
    }
}
