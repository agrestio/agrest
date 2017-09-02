package com.nhl.link.rest.meta;

import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.reflect.Accessor;
import org.apache.cayenne.reflect.PropertyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A collection of entity properties that are not derived from the object structure. An {@link LrEntityOverlay} is
 * provided to LinkRest by the app, and is merged into a corresponding {@link LrEntity}.
 *
 * @since 1.12
 */
public class LrEntityOverlay<T> {

    private Class<T> type;
    private Map<String, LrAttribute> attributes;
    private Map<String, Function<LrDataMap, LrRelationship>> relationships;

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

        Accessor accessor = PropertyUtils.accessor(name);

        // TODO: provide actual type instead of Object
        addAttribute(name, Object.class, accessor::getValue);
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
