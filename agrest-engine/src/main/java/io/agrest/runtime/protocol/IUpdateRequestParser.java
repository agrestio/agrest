package io.agrest.runtime.protocol;

import io.agrest.EntityUpdate;
import io.agrest.meta.AgEntity;

import java.io.InputStream;
import java.util.Collection;

/**
 * @since 5.0
 */
public interface IUpdateRequestParser {

    <T> Collection<EntityUpdate<T>> parse(AgEntity<T> entity, String entityData);

    <T> Collection<EntityUpdate<T>> parse(AgEntity<T> entity, InputStream entityStream);
}
