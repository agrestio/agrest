package io.agrest.meta;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.PropertyFilter;
import io.agrest.access.PropertyFilteringRulesBuilder;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.reader.DataReader;
import io.agrest.resolver.BaseRootDataResolver;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.resolver.RelatedDataResolver;
import io.agrest.resolver.RelatedDataResolverFactory;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.RootDataResolverFactory;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A mutable collection of entity properties that allow the application code to override and customize {@link AgEntity}
 * structure either globally or per-request.
 *
 * @since 1.12
 */
public class AgEntityOverlay<T> {

    private final Class<T> type;
    private final Map<String, AgAttributeOverlay> attributes;
    private final Map<String, AgRelationshipOverlay> relationships;
    private RootDataResolver<T> rootDataResolver;

    private PropertyFilter readablePropFilter;
    private PropertyFilter writablePropFilter;

    private boolean ignoreOverlaidReadFilter;
    private boolean ignoreOverlaidCreateAuthorizer;
    private boolean ignoreOverlaidUpdateAuthorizer;
    private boolean ignoreOverlaidDeleteAuthorizer;

    private ReadFilter<T> readFilter;
    private CreateAuthorizer<T> createAuthorizer;
    private UpdateAuthorizer<T> updateAuthorizer;
    private DeleteAuthorizer<T> deleteAuthorizer;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
        this.readFilter = ReadFilter.allowsAllFilter();
        this.createAuthorizer = CreateAuthorizer.allowsAllFilter();
        this.updateAuthorizer = UpdateAuthorizer.allowsAllFilter();
        this.deleteAuthorizer = DeleteAuthorizer.allowsAllFilter();
    }

    private static DataReader fromFunction(Function<?, ?> f) {
        // lose generics. DataReader is not parameterized
        Function fx = f;
        return fx::apply;
    }

    private static <T> RelatedDataResolver<T> resolverForReader(Function<?, T> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    static <T> RelatedDataResolver<T> resolverForListReader(Function<?, List<T>> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    /**
     * Resolves entity overlay to an entity.
     *
     * @since 5.0
     */
    public AgEntity<T> resolve(AgSchema schema, AgEntity<T> toOverlay, Collection<AgEntity<? extends T>> overlaidSubEntities) {

        Objects.requireNonNull(toOverlay);

        AgEntityOverlayResolver resolver = new AgEntityOverlayResolver(schema, toOverlay);

        getAttributeOverlays().forEach(resolver::loadAttributeOverlay);
        getRelationshipOverlays().forEach(resolver::loadRelationshipOverlay);

        if (readablePropFilter != null) {
            PropertyFilteringRulesBuilder pa = new PropertyFilteringRulesBuilder();
            readablePropFilter.apply(pa);
            pa.resolveInaccessible(toOverlay, this).forEach(resolver::setReadAccess);
        }

        if (writablePropFilter != null) {
            PropertyFilteringRulesBuilder pa = new PropertyFilteringRulesBuilder();
            writablePropFilter.apply(pa);
            pa.resolveInaccessible(toOverlay, this).forEach(resolver::setWriteAccess);
        }

        ReadFilter<T> readFilter = ignoreOverlaidReadFilter
                ? this.readFilter
                : toOverlay.getReadFilter().andThen(this.readFilter);

        CreateAuthorizer<T> createAuthorizer = ignoreOverlaidCreateAuthorizer
                ? this.createAuthorizer
                : toOverlay.getCreateAuthorizer().andThen(this.createAuthorizer);

        UpdateAuthorizer<T> updateAuthorizer = ignoreOverlaidUpdateAuthorizer
                ? this.updateAuthorizer
                : toOverlay.getUpdateAuthorizer().andThen(this.updateAuthorizer);

        DeleteAuthorizer<T> deleteAuthorizer = ignoreOverlaidDeleteAuthorizer
                ? this.deleteAuthorizer
                : toOverlay.getDeleteAuthorizer().andThen(this.deleteAuthorizer);

        return new DefaultEntity<>(
                toOverlay.getName(),
                type,
                toOverlay.isAbstract(),
                overlaidSubEntities,
                resolver.ids,
                resolver.attributes,
                resolver.relationships,
                rootDataResolver != null ? rootDataResolver : toOverlay.getDataResolver(),
                readFilter,
                createAuthorizer,
                updateAuthorizer,
                deleteAuthorizer
        );
    }

    public boolean isEmpty() {
        return rootDataResolver == null
                && attributes.isEmpty()
                && relationships.isEmpty()
                && readablePropFilter == null
                && writablePropFilter == null
                && !ignoreOverlaidReadFilter && readFilter.allowsAll()
                && !ignoreOverlaidCreateAuthorizer && createAuthorizer.allowsAll()
                && !ignoreOverlaidUpdateAuthorizer && updateAuthorizer.allowsAll()
                && !ignoreOverlaidDeleteAuthorizer && deleteAuthorizer.allowsAll();
    }

    /**
     * @since 5.0
     */
    public <S extends T> AgEntityOverlay<S> clone(Class<S> toType) {
        AgEntityOverlay<S> clone = new AgEntityOverlay<>(toType);

        clone.rootDataResolver = (RootDataResolver<S>) this.rootDataResolver;
        clone.attributes.putAll(this.attributes);
        clone.relationships.putAll(this.relationships);
        clone.readablePropFilter = this.readablePropFilter;
        clone.writablePropFilter = this.writablePropFilter;

        clone.ignoreOverlaidReadFilter = this.ignoreOverlaidReadFilter;
        clone.readFilter = (ReadFilter<S>) this.readFilter;

        clone.ignoreOverlaidCreateAuthorizer = this.ignoreOverlaidCreateAuthorizer;
        clone.createAuthorizer = (CreateAuthorizer<S>) this.createAuthorizer;

        clone.ignoreOverlaidUpdateAuthorizer = this.ignoreOverlaidUpdateAuthorizer;
        clone.updateAuthorizer = (UpdateAuthorizer<S>) this.updateAuthorizer;

        clone.ignoreOverlaidDeleteAuthorizer = this.ignoreOverlaidDeleteAuthorizer;
        clone.deleteAuthorizer = (DeleteAuthorizer<S>) this.deleteAuthorizer;

        return clone;
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

        if (anotherOverlay.readablePropFilter != null) {
            readablePropFilter(anotherOverlay.readablePropFilter);
        }

        if (anotherOverlay.writablePropFilter != null) {
            writablePropFilter(anotherOverlay.writablePropFilter);
        }

        if (anotherOverlay.getRootDataResolver() != null) {
            this.rootDataResolver = anotherOverlay.getRootDataResolver();
        }

        // When merging "ignores", a "true" on either side of the merge results in "true" in the merged version
        this.ignoreOverlaidReadFilter = anotherOverlay.ignoreOverlaidReadFilter || this.ignoreOverlaidReadFilter;
        this.ignoreOverlaidCreateAuthorizer = anotherOverlay.ignoreOverlaidCreateAuthorizer || this.ignoreOverlaidCreateAuthorizer;
        this.ignoreOverlaidUpdateAuthorizer = anotherOverlay.ignoreOverlaidUpdateAuthorizer || this.ignoreOverlaidUpdateAuthorizer;
        this.ignoreOverlaidDeleteAuthorizer = anotherOverlay.ignoreOverlaidDeleteAuthorizer || this.ignoreOverlaidDeleteAuthorizer;

        // When merging filters/authorizers, "ignores" are themselves ignored, and will only have effect on the underlying entity.
        // This allows to combine multiple overlays in the same scope (e.g. request or AgRuntimeBuilder) without them
        // messing up each other, and only override things between the scopes

        this.readFilter = this.readFilter.andThen(anotherOverlay.readFilter);
        this.createAuthorizer = this.createAuthorizer.andThen(anotherOverlay.createAuthorizer);
        this.updateAuthorizer = this.updateAuthorizer.andThen(anotherOverlay.updateAuthorizer);
        this.deleteAuthorizer = this.deleteAuthorizer.andThen(anotherOverlay.deleteAuthorizer);

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
     * Adds a {@link PropertyFilter} to any existing read filters in this overlay.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> readablePropFilter(PropertyFilter filter) {
        this.readablePropFilter = readablePropFilter != null ? readablePropFilter.andThen(filter) : filter;
        return this;
    }

    /**
     * Adds a {@link PropertyFilter} to any existing write filters in this overlay.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> writablePropFilter(PropertyFilter filter) {
        this.writablePropFilter = writablePropFilter != null ? writablePropFilter.andThen(filter) : filter;
        return this;
    }


    /**
     * If called, overlaid AgEntity read filter will be ignored. Otherwise, it will be combined with the overlay
     * read filter.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> ignoreOverlaidReadFilter() {
        this.ignoreOverlaidReadFilter = true;
        return this;
    }

    /**
     * If called, overlaid AgEntity create authorizer will be ignored. Otherwise, it will be combined with the overlay
     * create authorizer.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> ignoreOverlaidCreateAuthorizer() {
        this.ignoreOverlaidCreateAuthorizer = true;
        return this;
    }

    /**
     * If called, overlaid AgEntity update authorizer will be ignored. Otherwise, it will be combined with the overlay
     * update authorizer.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> ignoreOverlaidUpdateAuthorizer() {
        this.ignoreOverlaidUpdateAuthorizer = true;
        return this;
    }

    /**
     * If called, overlaid AgEntity delete authorizer will be ignored. Otherwise, it will be combined with the overlay
     * delete authorizer.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> ignoreOverlaidDeleteAuthorizer() {
        this.ignoreOverlaidDeleteAuthorizer = true;
        return this;
    }

    /**
     * Adds an object READ filter to the existing filters.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> readFilter(ReadFilter<T> filter) {
        this.readFilter = this.readFilter.andThen(filter);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> createAuthorizer(CreateAuthorizer<T> authorizer) {
        this.createAuthorizer = this.createAuthorizer.andThen(authorizer);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> updateAuthorizer(UpdateAuthorizer<T> authorizer) {
        this.updateAuthorizer = this.updateAuthorizer.andThen(authorizer);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> deleteAuthorizer(DeleteAuthorizer<T> authorizer) {
        this.deleteAuthorizer = this.deleteAuthorizer.andThen(authorizer);
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #attribute(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        return attribute(name, valueType, reader);
    }

    /**
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> attribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAttributeOverlay(name, type, valueType, null, null, fromFunction(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #attribute(String, Class, boolean, boolean, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, boolean readable, boolean writable, Function<T, V> reader) {
        return attribute(name, valueType, readable, writable, reader);
    }

    /**
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> attribute(String name, Class<V> valueType, boolean readable, boolean writable, Function<T, V> reader) {
        attributes.put(name, new DefaultAttributeOverlay(name, type, valueType, readable, writable, fromFunction(reader)));
        return this;
    }

    /**
     * Redefines related resolver for a named relationship.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> relatedDataResolver(String name, RelatedDataResolverFactory resolverFactory) {
        RelatedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultRelationshipOverlay(name, type, null, null, null, null, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> relatedDataResolver(String name, Function<T, ?> reader) {
        relationships.put(name, new DefaultRelationshipOverlay(name, type, null, null, null, null, resolverForReader(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toOne(String, Class, RelatedDataResolverFactory)}
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, RelatedDataResolverFactory resolverFactory) {
        return toOne(name, targetType, resolverFactory);
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toOne(String name, Class<V> targetType, RelatedDataResolverFactory resolverFactory) {
        RelatedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, false, null, null, resolver));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toOne(String, Class, boolean, boolean, RelatedDataResolverFactory)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, boolean readable, boolean writable, RelatedDataResolverFactory resolverFactory) {
        return toOne(name, targetType, readable, writable, resolverFactory);
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toOne(String name, Class<V> targetType, boolean readable, boolean writable, RelatedDataResolverFactory resolverFactory) {
        RelatedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, false, readable, writable, resolver));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toOne(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, Function<T, V> reader) {
        return toOne(name, targetType, reader);
    }

    /**
     * Adds or replaces a to-one relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toOne(String name, Class<V> targetType, Function<T, V> reader) {
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, false, null, null, resolverForReader(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toOne(String, Class, boolean, boolean, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, V> reader) {
        return toOne(name, targetType, readable, writable, reader);
    }

    /**
     * Adds or replaces a to-one relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toOne(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, V> reader) {
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, false, readable, writable, resolverForReader(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toMany(String, Class, RelatedDataResolverFactory)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, RelatedDataResolverFactory resolverFactory) {
        return toMany(name, targetType, resolverFactory);
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toMany(String name, Class<V> targetType, RelatedDataResolverFactory resolverFactory) {
        RelatedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, true, null, null, resolver));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toMany(String, Class, boolean, boolean, RelatedDataResolverFactory)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, boolean readable, boolean writable, RelatedDataResolverFactory resolverFactory) {
        return toMany(name, targetType, readable, writable, resolverFactory);
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toMany(String name, Class<V> targetType, boolean readable, boolean writable, RelatedDataResolverFactory resolverFactory) {
        RelatedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, true, readable, writable, resolver));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toMany(String, Class, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, Function<T, List<V>> reader) {
        return toMany(name, targetType, reader);
    }

    /**
     * Adds or replaces a to-many relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toMany(String name, Class<V> targetType, Function<T, List<V>> reader) {
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, true, null, null, resolverForListReader(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #toMany(String, Class, boolean, boolean, Function)}
     */
    @Deprecated
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, List<V>> reader) {
        return toMany(name, targetType, readable, writable, reader);
    }

    /**
     * Adds or replaces a to-many relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 5.0
     */
    public <V> AgEntityOverlay<T> toMany(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, List<V>> reader) {
        relationships.put(name, new DefaultRelationshipOverlay(name, type, targetType, true, readable, writable, resolverForListReader(reader)));
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #dataResolverFactory(RootDataResolverFactory)}
     */
    @Deprecated
    public AgEntityOverlay<T> redefineDataResolverFactory(RootDataResolverFactory rootDataResolverFactory) {
        return dataResolverFactory(rootDataResolverFactory);
    }

    /**
     * @since 5.0
     */
    public AgEntityOverlay<T> dataResolverFactory(RootDataResolverFactory rootDataResolverFactory) {
        this.rootDataResolver = rootDataResolverFactory.resolver(type);
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #dataResolver(RootDataResolver)}
     */
    @Deprecated
    public AgEntityOverlay<T> redefineDataResolver(RootDataResolver<T> rootDataResolver) {
        return dataResolver(rootDataResolver);
    }

    /**
     * @since 5.0
     */
    public AgEntityOverlay<T> dataResolver(RootDataResolver<T> rootDataResolver) {
        this.rootDataResolver = rootDataResolver;
        return this;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #dataResolver(Function)}
     */
    @Deprecated
    public AgEntityOverlay<T> redefineDataResolver(Function<SelectContext<T>, List<T>> reader) {
        return dataResolver(reader);
    }

    /**
     * Redefines entity {@link RootDataResolver} by using the provided function.
     *
     * @since 5.0
     */
    public AgEntityOverlay<T> dataResolver(Function<SelectContext<T>, List<T>> reader) {
        this.rootDataResolver = new BaseRootDataResolver<>() {

            @Override
            protected List<T> doFetchData(SelectContext<T> context) {
                return reader.apply(context);
            }
        };
        return this;
    }
}
