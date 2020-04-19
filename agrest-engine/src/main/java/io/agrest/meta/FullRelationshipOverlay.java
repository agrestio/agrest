package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

/**
 * {@link AgRelationshipOverlay} that internally defines full relationship semantics, and can either redefine an existing
 * relationship or introduce an entirely new one.
 *
 * @since 3.4
 */
public class FullRelationshipOverlay implements AgRelationshipOverlay {

    private String name;
    private Class<?> targetType;
    private boolean toMany;
    private NestedDataResolver<?> resolver;

    public FullRelationshipOverlay(
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
    public AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap) {
        AgEntity<?> targetEntity = agDataMap.getEntity(targetType);
        return new DefaultAgRelationship(name, targetEntity, toMany, resolver);
    }
}
