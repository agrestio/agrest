package io.agrest.meta;

import io.agrest.meta.compiler.BeanAnalyzer;
import io.agrest.meta.compiler.PropertyGetter;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.ParentPropertyDataResolvers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A mutable collection of entity properties that are not derived from the object structure. {@link AgEntityOverlay}
 * objects are provided to Agrest by the app, and are merged into corresponding {@link AgEntity} entities.
 *
 * @since 1.12
 */
public class AgEntityOverlay<T> {

    private Class<T> type;
    private Map<String, AgAttribute> attributes;
    private Map<String, AgRelationshipOverlay> relationships;
    private Map<String, PropertyGetter> typeGetters;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    // lose generics ... PropertyReader is not parameterized
    private static PropertyReader asPropertyReader(Function reader) {
        return (o, n) -> reader.apply(o);
    }

    /**
     * @since 2.10
     */
    public AgEntityOverlay<T> merge(AgEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);
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
    public AgRelationshipOverlay getRelationship(String name) {
        return relationships.get(name);
    }

    /**
     * @since 3.4
     */
    public Iterable<AgRelationshipOverlay> getRelationships() {
        return relationships.values();
    }

    private Map<String, PropertyGetter> getTypeGetters() {

        // compile getters map lazily, only when the caller adds attributes that require getters
        if (this.typeGetters == null) {
            Map<String, PropertyGetter> getters = new HashMap<>();

            // TODO: this is expensive, and since #422 this may be called per-request..
            //  Need either a stack-scoped caching strategy or deprecating the caller - "addAttribute"
            BeanAnalyzer.findGetters(type).forEach(pm -> getters.put(pm.getName(), pm));

            this.typeGetters = getters;
        }

        return typeGetters;
    }

    /**
     * Adds an attribute to the overlaid entity. Type and value reader are determined via class introspection, so this
     * method may be quite slow. Consider using {@link #addAttribute(String, Class, Function)} instead.
     *
     * @since 2.10
     */
    public AgEntityOverlay<T> addAttribute(String name) {

        PropertyGetter getter = getTypeGetters().get(name);

        if (getter == null) {
            throw new IllegalArgumentException("'" + name + "' is not a readable property in " + type.getName());
        }

        Class vType = getter.getType();
        addAttribute(name, vType, getter::getValue);
        return this;
    }

    /**
     * Adds an "ad-hoc" attribute to the overlaid entity. The value of the attribute will be calculated from
     * each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> AgEntityOverlay<T> addAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttribute(name, valueType, asPropertyReader(reader)));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> amendRelationship(String name, NestedDataResolver<V> resolver) {
        relationships.put(name, new PartialRelationshipOverlay(type, name, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> amendRelationship(String name, Function<T, ?> reader) {
        amendRelationship(name, ParentPropertyDataResolvers.forReader(reader));
        return this;
    }

    /**
     * Adds a relationship overlay to the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> addOrAmendToOne(String name, Class<V> targetType, NestedDataResolver<V> resolver) {
        relationships.put(name, new FullRelationshipOverlay(name, targetType, false, resolver));
        return this;
    }

    /**
     * Adds a relationship overlay to the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> addOrAmendToMany(String name, Class<V> targetType, NestedDataResolver<V> resolver) {
        relationships.put(name, new FullRelationshipOverlay(name, targetType, true, resolver));
        return this;
    }

    /**
     * Adds a to-one relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> addOrAmendToOne(String name, Class<V> targetType, Function<T, V> reader) {
        return addOrAmendToOne(name, targetType, ParentPropertyDataResolvers.forReader(reader));
    }

    /**
     * Adds a to-many relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> addOrAmendToMany(String name, Class<V> targetType, Function<T, List<V>> reader) {
        return addOrAmendToMany(name, targetType, ParentPropertyDataResolvers.forListReader(reader));
    }

    /**
     * @deprecated since 3.4 in favor of {@link #addOrAmendToOne(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> addToOneRelationship(String name, Class<V> targetType, Function<T, V> reader) {
        return addOrAmendToOne(name, targetType, reader);
    }

    /**
     * @deprecated since 3.4 in favor of {@link #addOrAmendToMany(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> addToManyRelationship(String name, Class<V> targetType, Function<T, List<V>> reader) {
        return addOrAmendToMany(name, targetType, reader);
    }
}
