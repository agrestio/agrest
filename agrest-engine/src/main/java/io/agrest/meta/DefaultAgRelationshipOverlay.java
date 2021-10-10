package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

/**
 * {@link AgRelationshipOverlay} that internally defines full relationship semantics, and can either redefine an existing
 * relationship or introduce an entirely new one.
 *
 * @since 3.4
 */
public class DefaultAgRelationshipOverlay extends BasePropertyOverlay implements AgRelationshipOverlay {

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

        super(name, sourceType);

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

        // we can't use defaults from the overlaid relationship, so make sure we have all the required ones present,
        // and provide defaults where possible

        return new DefaultAgRelationship(name,
                agDataMap.getEntity(requiredProperty("targetType", targetType)),
                requiredProperty("toMany", toMany),

                // using the defaults from @AgRelationship annotation
                propertyOrDefault(readable, true),
                propertyOrDefault(writable, true),

                requiredProperty("resolver", resolver));
    }
}
