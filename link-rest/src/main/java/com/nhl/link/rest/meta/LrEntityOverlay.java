package com.nhl.link.rest.meta;

import com.nhl.link.rest.meta.compiler.BeanAnalyzer;
import com.nhl.link.rest.meta.compiler.PropertyGetter;
import com.nhl.link.rest.property.PropertyReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A mutable collection of entity properties that are not derived from the object structure. {@link LrEntityOverlay}
 * objects are provided to LinkRest by the app, and are merged into corresponding {@link LrEntity} entities.
 *
 * @since 1.12
 */
public class LrEntityOverlay<T> {

    private Class<T> type;
    private Map<String, LrAttribute> attributes;
    private Map<String, Function<LrDataMap, LrRelationship>> relationships;
    private Map<String, PropertyGetter> typeGetters;

    public LrEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    /**
     * @since 2.10
     */
    public LrEntityOverlay<T> merge(LrEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);
        return this;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 2.10
     */
    public Iterable<LrAttribute> getAttributes() {
        return attributes.values();
    }

    /**
     * @since 2.10
     */
    public Stream<LrRelationship> getRelatonships(LrDataMap dataMap) {
        // resolve relationship targets
        return relationships.values().stream().map(f -> f.apply(dataMap));
    }

    private Map<String, PropertyGetter> getTypeGetters() {

        // compile getters map lazily, only when the caller adds attributes that require getters
        if (this.typeGetters == null) {
            Map<String, PropertyGetter> getters = new HashMap<>();

            // this is expensive... still not caching, as presumably overlays are processed only once
            BeanAnalyzer.findGetters(type).forEach(pm -> getters.put(pm.getName(), pm));

            this.typeGetters = getters;
        }

        return typeGetters;
    }

    /**
     * Adds an attribute to the overlaid entity. The value of the attribute will be read from the object itself.
     * This overlay is only needed if LinkRest can't otherwise determine property presence in the entity.
     * An alternative to calling this method explicitly is annotating property getters with
     * {@link com.nhl.link.rest.annotation.LrAttribute}, {@link com.nhl.link.rest.annotation.LrRelationship} or
     * {@link com.nhl.link.rest.annotation.LrId}. Also all Cayenne attributes are automatically added to the entity.
     *
     * @since 2.10
     */
    public LrEntityOverlay<T> addAttribute(String name) {

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
     * each entity object by applying the "valueSupplier" function. This allows LinkRest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> LrEntityOverlay<T> addAttribute(String name, Class<V> valueType, Function<T, V> valueSupplier) {
        PropertyReader reader = PropertyReader.forValueProducer(valueSupplier);
        attributes.put(name, new DefaultLrAttribute(name, valueType, reader));
        return this;
    }

    /**
     * Adds an "ad-hoc" to-one relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "valueSupplier" function. This allows LinkRest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> LrEntityOverlay<T> addToOneRelationship(String name, Class<V> valueType, Function<T, V> valueSupplier) {
        relationships.put(name, dm -> resolveToOne(dm, name, valueType, valueSupplier));
        return this;
    }

    /**
     * Adds an "ad-hoc" to-many relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "valueSupplier" function. This allows LinkRest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> LrEntityOverlay<T> addToManyRelationship(String name, Class<V> valueType, Function<T, List<V>> valueSupplier) {
        relationships.put(name, dm -> resolveToMany(dm, name, valueType, valueSupplier));
        return this;
    }

    private <V> LrRelationship resolveToOne(LrDataMap dataMap, String name, Class<V> type, Function<T, V> valueSupplier) {
        LrEntity<V> target = dataMap.getEntity(type);
        PropertyReader reader = PropertyReader.forValueProducer(valueSupplier);
        return new DefaultLrRelationship(name, target, false, reader);
    }

    private <V> LrRelationship resolveToMany(LrDataMap dataMap, String name, Class<V> type, Function<T, List<V>> valueSupplier) {
        LrEntity<V> target = dataMap.getEntity(type);
        PropertyReader reader = PropertyReader.forValueProducer(valueSupplier);
        return new DefaultLrRelationship(name, target, true, reader);
    }
}
