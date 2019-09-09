package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.property.PropertyReader;

import java.util.function.Function;

/**
 * @since 3.4
 */
public interface AgRelationshipOverlay {

    String getName();

    Class<?> getTargetType();

    boolean isToMany();

    Function<ResourceEntity<?>, PropertyReader> getReaderFactory();
}
