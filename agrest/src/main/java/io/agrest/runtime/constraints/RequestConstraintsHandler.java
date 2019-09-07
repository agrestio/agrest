package io.agrest.runtime.constraints;

import io.agrest.EntityUpdate;
import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.constraints.ConstrainedAgEntity;
import io.agrest.constraints.Constraint;
import io.agrest.meta.AgAttribute;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @since 1.6
 */
class RequestConstraintsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestConstraintsHandler.class);

    RequestConstraintsHandler() {
    }

    <T> boolean constrainResponse(ResourceEntity<T> resourceEntity, Constraint<T> c) {

        // Null entity means we don't need to worry about unauthorized
        // attributes and relationships
        if (resourceEntity == null) {
            return true;
        }

        if (c == null) {
            return false;
        }

        applyForRead(resourceEntity, c.apply(resourceEntity.getAgEntity()));
        return true;
    }

    <T> boolean constrainUpdate(UpdateContext<T> context, Constraint<T> c) {

        if (context.getUpdates().isEmpty()) {
            return true;
        }

        if (c == null) {
            return false;
        }

        applyForWrite(context, c.apply(context.getEntity().getAgEntity()));
        return true;
    }

    private void applyForWrite(UpdateContext<?> context, ConstrainedAgEntity constraints) {

        if (!constraints.isIdIncluded()) {
            context.setIdUpdatesDisallowed(true);
        }

        // updates are not hierarchical yet, so simply check attributes...
        // TODO: updates may contain FKs ... need to handle that

        for (EntityUpdate<?> u : context.getUpdates()) {

            // exclude disallowed attributes
            Iterator<Entry<String, Object>> it = u.getValues().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Object> e = it.next();
                if (!constraints.hasAttribute(e.getKey())) {

                    // do not report default properties, as this wasn't a
                    // client's fault it go there..
                    if (!context.getEntity().isDefault(e.getKey())) {
                        LOGGER.info("Attribute not allowed, removing: {} for id {}", e.getKey(), u.getId());
                    }

                    it.remove();
                }
            }

            Iterator<String> it2 = u.getRelatedIds().keySet().iterator();
            while (it2.hasNext()) {
                String relationship = it2.next();
                if (!constraints.hasChild(relationship)) {
                    LOGGER.info("Relationship not allowed, removing: {} for id {}", relationship, u.getId());
                    it2.remove();
                }
            }
        }
    }

    private void applyForRead(ResourceEntity<?> target, ConstrainedAgEntity constraints) {

        if (!constraints.isIdIncluded()) {
            target.excludeId();
        }

        Iterator<AgAttribute> ait = target.getAttributes().values().iterator();
        while (ait.hasNext()) {

            AgAttribute a = ait.next();
            if (!constraints.hasAttribute(a.getName())) {

                // do not report default properties, as this wasn't a client's
                // fault it go there..
                if (!target.isDefault(a.getName())) {
                    LOGGER.info("Attribute not allowed, removing: {}", a.getName());
                }

                ait.remove();
            }
        }

        Iterator<Entry<String, ResourceEntity<?>>> rit = target.getChildren().entrySet().iterator();
        while (rit.hasNext()) {

            Entry<String, ResourceEntity<?>> e = rit.next();
            ConstrainedAgEntity sourceChild = constraints.getChild(e.getKey());
            if (sourceChild != null) {

                // removing recursively ... the depth or recursion depends on
                // the depth of target, which is server-controlled. So it should
                // be a reasonably safe operation in regard to stack overflow
                applyForRead(e.getValue(), sourceChild);
            } else {

                // do not report default properties, as this wasn't a client's
                // fault it go there..
                if (!target.isDefault(e.getKey())) {
                    LOGGER.info("Relationship not allowed, removing: {}", e.getKey());
                }

                rit.remove();
            }
        }

        if (constraints.getQualifier() != null) {
            target.andQualifier(constraints.getQualifier());
        }

        // process 'mapByPath' ... treat it as a regular relationship/attribute
        // path.. Ignoring 'mapBy', presuming it matches the path. This way we
        // can simply check for one single path, not for all attributes in the
        // entities involved.

        if (target.getMapByPath() != null && !allowedMapBy(constraints, target.getMapByPath())) {
            LOGGER.info("'mapBy' not allowed, removing: {}", target.getMapByPath());
            target.mapBy(null, null);
        }
    }

    private boolean allowedMapBy(ConstrainedAgEntity source, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Status.BAD_REQUEST, "Path starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Status.BAD_REQUEST, "Path ends with dot: " + path);
        }

        if (dot > 0) {
            // process intermediate component
            String property = path.substring(0, dot);
            ConstrainedAgEntity child = source.getChild(property);
            return child != null && allowedMapBy(child, path.substring(dot + 1));

        } else {
            return allowedMapBy_LastComponent(source, path);
        }
    }

    private boolean allowedMapBy_LastComponent(ConstrainedAgEntity source, String path) {

        // process last component
        String property = path;

        if (property == null || property.length() == 0 || property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            return source.isIdIncluded();
        }

        if (source.hasAttribute(property)) {
            return true;
        }

        ConstrainedAgEntity child = source.getChild(property);
        return child != null && allowedMapBy_LastComponent(child, null);
    }
}
