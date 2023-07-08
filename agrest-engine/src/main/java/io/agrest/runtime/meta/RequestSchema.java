package io.agrest.runtime.meta;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgSchema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A mutable, partially-consistent view of an {@link io.agrest.meta.AgSchema} with a set of request-level overlays.
 * "Partially-consistent", because relationships in this schema would still point to un-overlaid entities.
 *
 * @since 5.0
 */
// TODO: is there a cheap way to connect relationships to overlaid entities?
public class RequestSchema {

    private final AgSchema schema;

    private Map<Class<?>, AgEntityOverlay<?>> overlays;
    private Map<Class<?>, AgEntity<?>> entities;

    public RequestSchema(AgSchema schema) {
        this.schema = schema;
    }

    /**
     * Returns potentially (and partially) overlaid entity. Note that the relationships in the returned entity
     * may still point to entities from the original AgSchema.
     */
    public <A> AgEntity<A> getEntity(Class<A> type) {
        return (AgEntity<A>) mutableEntities().computeIfAbsent(type, this::createEntity);
    }

    public <A> void addOverlay(AgEntityOverlay<A> overlay) {
        AgEntityOverlay<A> base = (AgEntityOverlay<A>) mutableOverlays()
                .computeIfAbsent(overlay.getType(), AgEntityOverlay::new);
        base.merge(overlay);
    }

    private <A> AgEntity<A> createEntity(Class<A> type) {
        return schema.getEntity(type).resolveOverlayHierarchy(schema, immutableOverlays());
    }

    private Map<Class<?>, AgEntity<?>> mutableEntities() {
        return entities == null ? (entities = new HashMap<>()) : entities;
    }

    private Map<Class<?>, AgEntityOverlay<?>> mutableOverlays() {
        return overlays == null ? (overlays = new HashMap<>()) : overlays;
    }

    private Map<Class<?>, AgEntityOverlay<?>> immutableOverlays() {
        return overlays == null ? Collections.emptyMap() : overlays;
    }
}
