package io.agrest.constraints;

import io.agrest.PathConstants;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;

import java.util.List;
import java.util.function.Function;

/**
 * A immutable builder of read or write constraints on a given entity. Constraints are predefined on the server and
 * are applied to requests, ensuring a client can't read or write more data than she is allowed to. Each builder method
 * in this class returns a new copy of Constraints, so it is safe to reuse instances including intermediate instances.
 *
 * @since 1.3
 */
public class ConstraintsBuilder<T> implements Constraint<T> {

    protected Function<ConstrainedAgEntity<T>, ConstrainedAgEntity<T>> op;

    protected ConstraintsBuilder(Function<ConstrainedAgEntity<T>, ConstrainedAgEntity<T>> op) {
        this.op = op;
    }

    @Override
    public ConstrainedAgEntity<T> apply(AgEntity<T> agEntity) {
        return op.apply(new ConstrainedAgEntity<T>(agEntity));
    }

    /**
     * @param attributeOrRelationship a name of the property to exclude.
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T> excludeProperty(String attributeOrRelationship) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeProperties(attributeOrRelationship);
            return ce;
        }));
    }

    /**
     * Excludes an attribute or relationship.
     *
     * @param attributeOrRelationship a name of the property to exclude.
     * @return a new instance of Constraints.
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #excludeProperty(String)}.
     */
    @Deprecated
    public ConstraintsBuilder<T> excludeProperty(Property<?> attributeOrRelationship) {
        return excludeProperty(attributeOrRelationship.getName());
    }

    /**
     * @param attributesOrRelationships an array of properties to exclude.
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T> excludeProperties(String... attributesOrRelationships) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeProperties(attributesOrRelationships);
            return ce;
        }));
    }

    /**
     * @param attributesOrRelationships an array of properties to exclude.
     * @return a new instance of Constraints.
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #excludeProperties(String...)}
     */
    @Deprecated
    public ConstraintsBuilder<T> excludeProperties(Property<?>... attributesOrRelationships) {

        String[] names = new String[attributesOrRelationships.length];
        for (int i = 0; i < attributesOrRelationships.length; i++) {
            names[i] = attributesOrRelationships[i].getName();
        }

        return excludeProperties(names);
    }

    /**
     * Excludes all previously included attributes.
     *
     * @return a new instance of Constraints.
     */
    public ConstraintsBuilder<T> excludeAllAttributes() {
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
    public ConstraintsBuilder<T> excludeAllChildren() {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.excludeAllChildren();
            return ce;
        }));
    }

    public ConstraintsBuilder<T> attribute(String attribute) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAttributes(attribute);
            return ce;
        }));
    }

    /**
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #attribute(String)}.
     */
    @Deprecated
    public ConstraintsBuilder<T> attribute(Property<?> attribute) {
        return attribute(attribute.getName());
    }

    public ConstraintsBuilder<T> allAttributes() {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAllAttributes();
            return ce;
        }));
    }

    public ConstraintsBuilder<T> attributes(String... attributes) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeAttributes(attributes);
            return ce;
        }));
    }

    /**
     * @deprecated since 3.6 as it uses Cayenne API in the method signature. Use {@link #attributes(String...)}.
     */
    @Deprecated
    public ConstraintsBuilder<T> attributes(Property<?>... attributes) {
        String[] names = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            names[i] = attributes[i].getName();
        }

        return attributes(names);
    }

    public ConstraintsBuilder<T> includeId(boolean include) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.includeId(include);
            return ce;
        }));
    }

    public ConstraintsBuilder<T> includeId() {
        return includeId(true);
    }

    public ConstraintsBuilder<T> excludeId() {
        return includeId(false);
    }

    public ConstraintsBuilder<T> and(Expression qualifier) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.andQualifier(qualifier);
            return ce;
        }));
    }

    public ConstraintsBuilder<T> or(Expression qualifier) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            ce.orQualifier(qualifier);
            return ce;
        }));
    }

    public <S> ConstraintsBuilder<T> path(Property<S> path, ConstraintsBuilder<S> subentityBuilder) {
        return path(path.getName(), subentityBuilder);
    }

    public <S> ConstraintsBuilder<T> toManyPath(Property<List<S>> path, ConstraintsBuilder<S> subentityBuilder) {
        return path(path.getName(), subentityBuilder);
    }

    public <S> ConstraintsBuilder<T> path(String path, ConstraintsBuilder<S> subEntityBuilder) {
        return new ConstraintsBuilder<>(op.andThen(ce -> {
            subEntityBuilder.op.apply(getOrCreateChild(ce, path));
            return ce;
        }));
    }

    private <P, C> ConstrainedAgEntity<C> getOrCreateChild(ConstrainedAgEntity<P> parent, String path) {

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

        ConstrainedAgEntity<?> child = parent.getChild(relationship.getName());
        if (child == null) {
            AgEntity<?> targetEntity = relationship.getTargetEntity();
            child = new ConstrainedAgEntity(targetEntity);
            parent.getChildren().put(relationship.getName(), child);
        }

        return dot < 0 ? (ConstrainedAgEntity<C>) child : getOrCreateChild(child, path.substring(dot + 1));
    }
}
