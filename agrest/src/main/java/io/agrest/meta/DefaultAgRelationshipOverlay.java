package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.property.PropertyReader;

import java.util.function.Function;

/**
 * @since 3.4
 */
public class DefaultAgRelationshipOverlay implements AgRelationshipOverlay {

    private String name;
    private Class<?> targetType;
    private boolean toMany;
    private Function<ResourceEntity<?>, PropertyReader> readerFactory;

    public DefaultAgRelationshipOverlay(
            String name,
            Class<?> targetType,
            boolean toMany,
            Function<ResourceEntity<?>, PropertyReader> readerFactory) {

        this.name = name;
        this.targetType = targetType;
        this.toMany = toMany;
        this.readerFactory = readerFactory;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public boolean isToMany() {
        return toMany;
    }

    @Override
    public Function<ResourceEntity<?>, PropertyReader> getReaderFactory() {
        return readerFactory;
    }

    @Override
    public Class<?> getTargetType() {
        return targetType;
    }
}
