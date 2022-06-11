package io.agrest.meta;

import io.agrest.resolver.RelatedDataResolver;

/**
 * {@link AgRelationshipOverlay} that internally defines full relationship semantics, and can either redefine an existing
 * relationship or introduce an entirely new one.
 *
 * @since 5.0
 */
public class DefaultRelationshipOverlay extends BasePropertyOverlay implements AgRelationshipOverlay {

    private final Class<?> targetType;
    private final Boolean toMany;
    private final Boolean readable;
    private final Boolean writable;
    private final RelatedDataResolver<?> resolver;

    public DefaultRelationshipOverlay(
            String name,
            Class<?> sourceType,
            Class<?> targetType,
            Boolean toMany,
            Boolean readable,
            Boolean writable,
            RelatedDataResolver<?> resolver) {

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
    public AgRelationship resolve(AgRelationship maybeOverlaid, AgSchema schema) {
        return maybeOverlaid != null ? resolveOverlaid(maybeOverlaid, schema) : resolveNew(schema);
    }

    private AgRelationship resolveOverlaid(AgRelationship overlaid, AgSchema schema) {
        boolean toMany = this.toMany != null ? this.toMany : overlaid.isToMany();
        boolean readable = this.readable != null ? this.readable : overlaid.isReadable();
        boolean writable = this.writable != null ? this.writable : overlaid.isWritable();

        RelatedDataResolver<?> resolver = this.resolver != null ? this.resolver : overlaid.getDataResolver();
        AgEntity<?> targetEntity = this.targetType != null
                ? schema.getEntity(this.targetType)
                : overlaid.getTargetEntity();

        return new DefaultRelationship(name, targetEntity, toMany, readable, writable, resolver);
    }

    private AgRelationship resolveNew(AgSchema schema) {

        // we can't use defaults from the overlaid relationship, so make sure we have all the required ones present,
        // and provide defaults where possible

        return new DefaultRelationship(name,
                schema.getEntity(requiredProperty("targetType", targetType)),
                requiredProperty("toMany", toMany),

                // using the defaults from @AgRelationship annotation
                propertyOrDefault(readable, true),
                propertyOrDefault(writable, true),

                requiredProperty("resolver", resolver));
    }
}
