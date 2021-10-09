package io.agrest.meta;

import io.agrest.property.PropertyReader;
import io.agrest.resolver.*;

import java.util.*;
import java.util.function.Function;

/**
 * A mutable collection of entity properties that are not derived from the object structure. {@link AgEntityOverlay}
 * objects are provided by the app and are merged into corresponding {@link AgEntity} entities to customize their
 * structure.
 *
 * @since 1.12
 */
public class AgEntityOverlay<T> {

    private final Class<T> type;
    //  TODO: introduce AgAttributeOverride to allow for partial overrides, like changing a reader
    private final Map<String, AgAttributeOverlay> attributes;
    private final Map<String, AgRelationshipOverlay> relationships;
    private final List<String> excludes;
    private RootDataResolver<T> rootDataResolver;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
        this.excludes = new ArrayList<>(2);
    }

    private static PropertyReader fromFunction(Function<?, ?> f) {
        // lose generics. PropertyReader is not parameterized
        Function fx = f;
        return fx::apply;
    }

    private static <T> NestedDataResolver<T> resolverForReader(Function<?, T> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    static <T> NestedDataResolver<T> resolverForListReader(Function<?, List<T>> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    /**
     * Combines this overlay with another overlay. This overlay is modified, loading overlaid properties and
     * resolvers from another overlay.
     *
     * @return this overlay
     * @since 2.10
     */
    public AgEntityOverlay<T> merge(AgEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);
        excludes.addAll(anotherOverlay.excludes);
        if (anotherOverlay.getRootDataResolver() != null) {
            this.rootDataResolver = anotherOverlay.getRootDataResolver();
        }
        return this;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 4.7
     */
    public AgAttributeOverlay getAttributeOverlay(String name) {
        return attributes.get(name);
    }

    /**
     * @since 4.7
     */
    public Iterable<AgAttributeOverlay> getAttributeOverlays() {
        return attributes.values();
    }

    /**
     * @since 3.4
     */
    public AgRelationshipOverlay getRelationshipOverlay(String name) {
        return relationships.get(name);
    }

    /**
     * @since 3.4
     */
    public Iterable<AgRelationshipOverlay> getRelationshipOverlays() {
        return relationships.values();
    }

    /**
     * @since 3.4
     */
    public RootDataResolver<T> getRootDataResolver() {
        return rootDataResolver;
    }

    /**
     * Removes a named property (attribute or relationship) from overlaid AgEntity.
     *
     * @return this overlay instance
     * @since 3.7
     */
    public AgEntityOverlay<T> exclude(String... properties) {
        Collections.addAll(excludes, properties);
        return this;
    }

    /**
     * @since 3.7
     */
    public Iterable<String> getExcludes() {
        return excludes;
    }

    /**
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttributeOverlay(name, type, valueType, fromFunction(reader)));
        return this;
    }

    /**
     * Redefines nested resolver for a named relationship.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, null, null, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, Function<T, ?> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, null, null, resolverForReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, resolver));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, resolver));
        return this;
    }

    /**
     * Adds or replaces a to-one relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, Function<T, V> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, resolverForReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a to-many relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, Function<T, List<V>> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, resolverForListReader(reader)));
        return this;
    }

    /**
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRootDataResolver(RootDataResolverFactory rootDataResolverFactory) {
        this.rootDataResolver = rootDataResolverFactory.resolver(type);
        return this;
    }

    /**
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRootDataResolver(RootDataResolver<T> rootDataResolver) {
        this.rootDataResolver = rootDataResolver;
        return this;
    }
}
