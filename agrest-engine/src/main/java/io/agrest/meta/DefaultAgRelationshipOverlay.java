package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

import java.util.Objects;

/**
 * {@link AgRelationshipOverlay} that internally defines full relationship semantics, and can either redefine an existing
 * relationship or introduce an entirely new one.
 *
 * @since 3.4
 */
public class DefaultAgRelationshipOverlay implements AgRelationshipOverlay {

    private final String name;
    private final Class<?> sourceType;
    private final Class<?> targetType;
    private final Boolean toMany;
    private final Boolean readable;
    private final Boolean writable;
    private final NestedDataResolver<?> resolver;

    public DefaultAgRelationshipOverlay(
            String name,
            Class<?> sourceType,
            Class<?> targetType,
            Boolean toMany,
            Boolean readable,
            Boolean writable,
            NestedDataResolver<?> resolver) {

        this.name = Objects.requireNonNull(name);
        this.sourceType = Objects.requireNonNull(sourceType);

        // optional attributes. NULL means not overlaid
        this.targetType = targetType;
        this.toMany = toMany;
        this.readable = readable;
        this.writable = writable;
        this.resolver = resolver;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgRelationship resolve(AgRelationship maybeOverlaid, AgDataMap agDataMap) {
        return maybeOverlaid != null ? resolveOverlaid(maybeOverlaid, agDataMap) : resolveNew(agDataMap);
    }

    private AgRelationship resolveOverlaid(AgRelationship overlaid, AgDataMap agDataMap) {
        boolean toMany = this.toMany != null ? this.toMany : overlaid.isToMany();
        boolean readable = this.readable != null ? this.readable : overlaid.isReadable();
        boolean writable = this.writable != null ? this.writable : overlaid.isWritable();

        NestedDataResolver<?> resolver = this.resolver != null ? this.resolver : overlaid.getResolver();
        AgEntity<?> targetEntity = this.targetType != null
                ? agDataMap.getEntity(this.targetType)
                : overlaid.getTargetEntity();

        return new DefaultAgRelationship(name, targetEntity, toMany, readable, writable, resolver);
    }

    private AgRelationship resolveNew(AgDataMap agDataMap) {

        // we can't use properties from the overlaid relationship, so make sure we have all the required ones present
        checkPropertyDefined("targetType", targetType);
        checkPropertyDefined("toMany", toMany);
        checkPropertyDefined("readable", readable);
        checkPropertyDefined("writable", writable);
        checkPropertyDefined("resolver", resolver);

        AgEntity<?> targetEntity = agDataMap.getEntity(targetType);
        return new DefaultAgRelationship(name, targetEntity, toMany, readable, writable, resolver);
    }

    private void checkPropertyDefined(String property, Object value) {
        if (value == null) {
            String message = String.format(
                    "Overlay can't be resolved: '%s' is not defined and no overlaid relationship '%s.%s' exists",
                    property,
                    this.sourceType.getName(),
                    this.name);

            throw new IllegalStateException(message);
        }
    }
}
