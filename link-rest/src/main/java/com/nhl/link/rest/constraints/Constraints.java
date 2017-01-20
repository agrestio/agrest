package com.nhl.link.rest.constraints;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.parser.PathConstants;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.Property;

import java.util.List;
import java.util.function.Function;

/**
 * A immutable builder of read or write constraints on a given entity. Constraints are predefined on the server and
 * are applied to requests, ensuring a client can't read or write more data than she is allowed to. Each builder method
 * in this class returns a new copy of Constraints, so it is safe to reuse instances including intermediate instances.
 *
 * @since 2.4
 */
public class Constraints<T> implements Constraint<T> {

    protected Function<ConstrainedLrEntity<T>, ConstrainedLrEntity<T>> op;

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    public static <T> Constraints<T> excludeAll(Class<T> type) {
        return new Constraints<T>(Function.identity());
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    public static <T> Constraints<T> idOnly(Class<T> type) {
        return excludeAll(type).includeId();
    }

    /**
     * @param type a root type for constraints.
     * @param <T>  LinkRest entity type.
     * @return a new Constraints instance.
     */
    public static <T> Constraints<T> idAndAttributes(Class<T> type) {
        return excludeAll(type).includeId().allAttributes();
    }


    protected Constraints(Function<ConstrainedLrEntity<T>, ConstrainedLrEntity<T>> op) {
        this.op = op;
    }

    @Override
    public ConstrainedLrEntity<T> apply(LrEntity<T> lrEntity) {
        return op.apply(new ConstrainedLrEntity<T>(lrEntity));
    }

    /**
     * @param attributeOrRelationship a name of the property to exclude.
     * @return a new instance of Constraints.
     */
    public Constraints<T> excludeProperty(String attributeOrRelationship) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitExcludePropertiesConstraint(attributeOrRelationship);
            return ce;
        }));
    }

    /**
     * Excludes an attribute or relationship.
     *
     * @param attributeOrRelationship a name of the property to exclude.
     * @return a new instance of Constraints.
     */
    public Constraints<T> excludeProperty(Property<?> attributeOrRelationship) {
        return excludeProperty(attributeOrRelationship.getName());
    }

    /**
     * @param attributesOrRelationships an array of properties to exclude.
     * @return a new instance of Constraints.
     */
    public Constraints<T> excludeProperties(String... attributesOrRelationships) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitExcludePropertiesConstraint(attributesOrRelationships);
            return ce;
        }));
    }

    /**
     * @param attributesOrRelationships an array of properties to exclude.
     * @return a new instance of Constraints.
     */
    public Constraints<T> excludeProperties(Property<?>... attributesOrRelationships) {

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
    public Constraints<T> excludeAllAttributes() {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitExcludeAllAttributesConstraint();
            return ce;
        }));
    }

    /**
     * Excludes all previously included child configs.
     *
     * @return a new instance of Constraints.
     */
    public Constraints<T> excludeAllChildren() {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitExcludeAllChildrenConstraint();
            return ce;
        }));
    }

    public Constraints<T> attribute(String attribute) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitIncludeAttributesConstraint(attribute);
            return ce;
        }));
    }

    public Constraints<T> attribute(Property<?> attribute) {
        return attribute(attribute.getName());
    }

    public Constraints<T> allAttributes() {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitIncludeAllAttributesConstraint();
            return ce;
        }));
    }

    public Constraints<T> attributes(String... attributes) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitIncludeAttributesConstraint(attributes);
            return ce;
        }));
    }

    public Constraints<T> attributes(Property<?>... attributes) {
        String[] names = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            names[i] = attributes[i].getName();
        }

        return attributes(names);
    }

    public Constraints<T> includeId(boolean include) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitIncludeIdConstraint(include);
            return ce;
        }));
    }

    public Constraints<T> includeId() {
        return includeId(true);
    }

    public Constraints<T> excludeId() {
        return includeId(false);
    }

    public Constraints<T> and(Expression qualifier) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitAndQualifierConstraint(qualifier);
            return ce;
        }));
    }

    public Constraints<T> or(Expression qualifier) {
        return new Constraints<>(op.andThen(ce -> {
            ce.visitOrQualifierConstraint(qualifier);
            return ce;
        }));
    }

    public <S> Constraints<T> path(Property<S> path, Constraints<S> subentityBuilder) {
        return path(path.getName(), subentityBuilder);
    }

    public <S> Constraints<T> toManyPath(Property<List<S>> path, Constraints<S> subentityBuilder) {
        return path(path.getName(), subentityBuilder);
    }

    public <S> Constraints<T> path(String path, Constraints<S> subEntityBuilder) {
        return new Constraints<>(op.andThen(ce -> {
            subEntityBuilder.op.apply(getOrCreateChild(ce, path));
            return ce;
        }));
    }

    private <P, C> ConstrainedLrEntity<C> getOrCreateChild(ConstrainedLrEntity<P> parent, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        // sanity check..
        if (dot == 0) {
            throw new IllegalArgumentException("Invalid path, starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new IllegalArgumentException("Invalid path, ends with dot: " + path);
        }

        String pathSegment = dot > 0 ? path.substring(0, dot) : path;

        LrRelationship relationship = parent.getEntity().getRelationship(pathSegment);
        if (relationship == null) {
            throw new IllegalArgumentException("Path contains non-relationship component: " + pathSegment);
        }

        ConstrainedLrEntity<?> child = parent.getChild(relationship.getName());
        if (child == null) {
            LrEntity<?> targetEntity = relationship.getTargetEntity();
            child = new ConstrainedLrEntity(targetEntity);
            parent.getChildren().put(relationship.getName(), child);
        }

        return dot < 0 ? (ConstrainedLrEntity<C>) child : getOrCreateChild(child, path.substring(dot + 1));
    }
}
