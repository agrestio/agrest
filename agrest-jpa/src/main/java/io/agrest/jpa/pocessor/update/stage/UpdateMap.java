package io.agrest.jpa.pocessor.update.stage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agrest.EntityUpdate;

/**
 * @since 5.0
 */
class UpdateMap<T> {

    private final Map<Object, EntityUpdate<T>> withId;
    private final List<EntityUpdate<T>> noId;

    UpdateMap(Map<Object, EntityUpdate<T>> withId, List<EntityUpdate<T>> noId) {
        this.withId = withId;
        this.noId = noId;
    }

    EntityUpdate<T> remove(Object id) {
        return withId.remove(id);
    }

    Set<Object> getIds() {
        return withId.keySet();
    }

    Collection<EntityUpdate<T>> getWithId() {
        return withId.values();
    }

    List<EntityUpdate<T>> getNoId() {
        return noId;
    }
}
