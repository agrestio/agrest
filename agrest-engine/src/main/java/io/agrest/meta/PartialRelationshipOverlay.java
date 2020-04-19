package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * {@link AgRelationshipOverlay} that internally defines only partial relationship semantics (name and resolver), and
 * can only be used to redefine existing relationships.
 *
 * @since 3.4
 */
public class PartialRelationshipOverlay implements AgRelationshipOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialRelationshipOverlay.class);

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
        return maybeOverlaid != null ? doResolve(maybeOverlaid) : cantResolve();
    }

    private AgRelationship cantResolve() {

        // an argument can be made that we need to throw here, but unfortunately such an exception will be far removed
        // from the place where an incorrect overlay is defined, and sometimes may not even be detected. So we decided
        // to log a warning, and ignore the overlay instead of keeping a time bomb in the code

        LOGGER.warn("Partial overlay can't be resolved. No underlying relationship '{}.{}' is defined", sourceEntityType.getName(), name);
        return null;
    }

    private AgRelationship doResolve(AgRelationship maybeOverlaid) {
        return new DefaultAgRelationship(name, maybeOverlaid.getTargetEntity(), maybeOverlaid.isToMany(), resolver);
    }
}
