package io.agrest.runtime.entity;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;

import java.util.Collection;
import java.util.List;

/**
 * @since 5.0
 */
public interface IIdResolver {

    default AgObjectId resolve(AgEntity<?> entity, Object idValue) {
        return idValue != null ? resolve(entity, List.of(idValue)).getFirst() : null;
    }

    List<AgObjectId> resolve(AgEntity<?> entity, Collection<?> idValues);

}
