package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

import java.util.Objects;

/**
 * {@link AgRelationshipOverlay} that internally defines only partial relationship semantics (name and resolver), and
 * can only be used to redefine existing relationships.
 *
 * @since 3.4
 */
public class PartialRelationshipOverlay implements AgRelationshipOverlay {

    private Class<?> sourceEntityType;
    private String name;
    private NestedDataResolver<?> resolver;

    public PartialRelationshipOverlay(Class<?> sourceEntityType, String name, NestedDataResolver<?> resolver) {
        this.name = Objects.requireNonNull(name);
        this.resolver = Objects.requireNonNull(resolver);
        this.sourceEntityType = Objects.requireNonNull(sourceEntityType);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap) {

        if (maybeOverlaid == null) {
            throw new IllegalStateException("Partial overlay can't be resolved. No relationship: "
                    + sourceEntityType.getName() + "." + name);
        }

        return new DefaultAgRelationship(name, maybeOverlaid.getTargetEntity(), maybeOverlaid.isToMany(), resolver);
    }
}
