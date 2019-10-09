package io.agrest.meta;

import io.agrest.meta.compiler.BeanAnalyzer;
import io.agrest.meta.compiler.PropertyGetter;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.ParentPropertyDataResolvers;
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

    private Class<T> type;
    //  TODO: AgAttributeOverride to allow for partial overrides, like changing a reader
    private Map<String, AgAttribute> attributes;
    private Map<String, AgRelationshipOverlay> relationships;

    @Deprecated
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
    public AgRelationshipOverlay getRelationshipOverlay(String name) {
        return relationships.get(name);
    }

    /**
     * @since 3.4
     */
    public Iterable<AgRelationshipOverlay> getRelationshipOverlays() {
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
     * method may be quite slow. Consider using {@link #redefineAttribute(String, Class, Function)} instead.
     *
     * @since 2.10
     * @deprecated since 3.4 in favor of {@link #redefineAttribute(String, Class, Function)}, as this method does class
     * introspection and can be really slow.
     */
    @Deprecated
    public AgEntityOverlay<T> addAttribute(String name) {

        PropertyGetter getter = getTypeGetters().get(name);

        if (getter == null) {
            throw new IllegalArgumentException("'" + name + "' is not a readable property in " + type.getName());
        }

        Class vType = getter.getType();
        redefineAttribute(name, vType, getter::getValue);
        return this;
    }

    /**
     * @deprecated since 3.4 in favor for {@link #redefineAttribute(String, Class, Function)}.
     */
    @Deprecated
    public <V> AgEntityOverlay<T> addAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        return redefineAttribute(name, valueType, reader);
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
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, NestedDataResolver<?> resolver) {
        relationships.put(name, new PartialRelationshipOverlay(type, name, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, Function<T, ?> reader) {
        redefineRelationshipResolver(name, ParentPropertyDataResolvers.forReader(reader));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, NestedDataResolver<? super V> resolver) {
        relationships.put(name, new FullRelationshipOverlay(name, targetType, false, resolver));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, NestedDataResolver<? super V> resolver) {
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
        return redefineToOne(name, targetType, ParentPropertyDataResolvers.forReader(reader));
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
        return redefineToMany(name, targetType, ParentPropertyDataResolvers.forListReader(reader));
    }

    /**
     * @deprecated since 3.4 in favor of {@link #redefineToOne(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> addToOneRelationship(String name, Class<V> targetType, Function<T, V> reader) {
        return redefineToOne(name, targetType, reader);
    }

    /**
     * @deprecated since 3.4 in favor of {@link #redefineToMany(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> addToManyRelationship(String name, Class<V> targetType, Function<T, List<V>> reader) {
        return redefineToMany(name, targetType, reader);
    }
}
