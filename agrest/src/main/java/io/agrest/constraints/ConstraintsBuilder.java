package io.agrest.constraints;

import io.agrest.PathConstants;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.function.Function;

/**
 * A immutable builder of read or write constraints on a given entity. Constraints are predefined on the server and
 * are applied to requests, ensuring a client can't read or write more data than she is allowed to. Each builder method
 * in this class returns a new copy of Constraints, so it is safe to reuse instances including intermediate instances.
 *
 * @since 1.3
 */
public class ConstraintsBuilder<T, E> implements Constraint<T, E> {

    protected Function<ConstrainedAgEntity<T, E>, ConstrainedAgEntity<T, E>> op;

    protected ConstraintsBuilder(Function<ConstrainedAgEntity<T, E>, ConstrainedAgEntity<T, E>> op) {
        this.op = op;
    }

    @Override
    public ConstrainedAgEntity<T, E> apply(AgEntity<T> agEntity) {
        return op.apply(new ConstrainedAgEntity<T, E>(agEntity));
    }

    /**
     * @param attributeOrRelationship a name of the property to exclude.
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T, E> excludeProperty(String attributeOrRelationship) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeProperties(attributeOrRelationship);
            return ce;
        }));
    }

    /**
     * @param attributesOrRelationships an array of properties to exclude.
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T, E> excludeProperties(String... attributesOrRelationships) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeProperties(attributesOrRelationships);
            return ce;
        }));
    }

    /**
     * Excludes all previously included attributes.
     *
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T, E> excludeAllAttributes() {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeAllAttributes();
            return ce;
        }));
    }

    /**
     * Excludes all previously included child configs.
     *
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T, E> excludeAllChildren() {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeAllChildren();
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> attribute(String attribute) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAttributes(attribute);
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> allAttributes() {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAllAttributes();
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> attributes(String... attributes) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAttributes(attributes);
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> includeId(boolean include) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeId(include);
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> includeId() {
        return includeId(true);
    }

    public ConstraintsBuilder<T, E> excludeId() {
        return includeId(false);
    }

    public ConstraintsBuilder<T, E> and(E qualifier) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.andQualifier(qualifier);
            return ce;
        }));
    }

    public ConstraintsBuilder<T, E> or(E qualifier) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.orQualifier(qualifier);
            return ce;
        }));
    }

    public <S> ConstraintsBuilder<T, E> toManyPath(String path, ConstraintsBuilder<S, E> subentityBuilder) {
        return path(path, subentityBuilder);
    }

    public <S> ConstraintsBuilder<T, E> path(String path, ConstraintsBuilder<S, E> subEntityBuilder) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            subEntityBuilder.op.apply(getOrCreateChild(ce, path));
            return ce;
        }));
    }

    private <P, C> ConstrainedAgEntity<C, E> getOrCreateChild(ConstrainedAgEntity<P, E> parent, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        // sanity check..
        if (dot == 0) {
            throw new IllegalArgumentException("Invalid path, starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new IllegalArgumentException("Invalid path, ends with dot: " + path);
        }

        String pathSegment = dot > 0 ? path.substring(0, dot) : path;

        AgRelationship relationship = parent.getEntity().getRelationship(pathSegment);
        if (relationship == null) {
            throw new IllegalArgumentException("Path contains non-relationship component: " + pathSegment);
        }

        ConstrainedAgEntity<P, E> child = parent.getChild(relationship.getName());
        if (child == null) {
            AgEntity<?> targetEntity = relationship.getTargetEntity();
            child = new ConstrainedAgEntity(targetEntity);
            parent.getChildren().put(relationship.getName(), child);
        }

        return dot < 0 ? (ConstrainedAgEntity<C, E>) child : getOrCreateChild(child, path.substring(dot + 1));
    }
}
