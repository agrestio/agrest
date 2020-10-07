package io.agrest.meta;

import io.agrest.property.PropertyReader;
import io.agrest.resolver.*;
import org.apache.cayenne.exp.parser.ASTObjPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationshipOverlay> relationships;
    private RootDataResolver<T> rootDataResolver;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    // lose generics ... PropertyReader is not parameterized
    private static PropertyReader asPropertyReader(Function reader) {
        return (o, n) -> reader.apply(o);
    }

    private static <T> NestedDataResolver<T> resolverForReader(Function<?, T> reader) {
        // lose generics. PropertyReader is not parameterized
        return new ReaderBasedResolver<>((o, n) -> ((Function) reader).apply(o));
    }

    static <T> NestedDataResolver<T> resolverForListReader(Function<?, List<T>> reader) {
        // lose generics. PropertyReader is not parameterized
        return new ReaderBasedResolver<>((o, n) -> ((Function) reader).apply(o));
    }

    /**
     * @since 2.10
     */
    public AgEntityOverlay<T> merge(AgEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);
        if (anotherOverlay.getRootDataResolver() != null) {
            this.rootDataResolver = anotherOverlay.getRootDataResolver();
        }
        return this;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 3.4
     */
    public AgAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * @since 2.10
     */
    public Iterable<AgAttribute> getAttributes() {
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
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttribute(name, valueType, new ASTObjPath(name), asPropertyReader(reader)));
        return this;
    }

    /**
     * Redefines nested resolver for a named relationship.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new PartialRelationshipOverlay(type, name, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, Function<T, ?> reader) {
        relationships.put(name, new PartialRelationshipOverlay(type, name, resolverForReader(reader)));
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
        relationships.put(name, new FullRelationshipOverlay(name, targetType, false, resolver));
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
        relationships.put(name, new FullRelationshipOverlay(name, targetType, true, resolver));
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
        relationships.put(name, new FullRelationshipOverlay(name, targetType, false, resolverForReader(reader)));
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
        relationships.put(name, new FullRelationshipOverlay(name, targetType, true, resolverForListReader(reader)));
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
