package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

/**
 * @since 3.4
 */
public class DefaultAgRelationshipOverlay implements AgRelationshipOverlay {

    private String name;
    private Class<?> targetType;
    private boolean toMany;
    private NestedDataResolver<?> resolver;

    public DefaultAgRelationshipOverlay(
            String name,
            Class<?> targetType,
            boolean toMany,
            NestedDataResolver<?> resolver) {

        this.name = name;
        this.targetType = targetType;
        this.toMany = toMany;
        this.resolver = resolver;
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
    public NestedDataResolver<?> getResolver() {
        return resolver;
    }

    @Override
    public Class<?> getTargetType() {
        return targetType;
    }
}
