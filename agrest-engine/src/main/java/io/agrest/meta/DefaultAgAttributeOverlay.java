package io.agrest.meta;

import io.agrest.property.PropertyReader;

/**
 * @since 4.7
 */
public class DefaultAgAttributeOverlay extends BasePropertyOverlay implements AgAttributeOverlay {

    private final Class<?> javaType;
    private final Boolean readable;
    private final Boolean writable;
    private final PropertyReader propertyReader;

    public DefaultAgAttributeOverlay(
            String name,
            Class<?> sourceType,
            Class<?> javaType,
            Boolean readable,
            Boolean writable,
            PropertyReader propertyReader) {

        super(name, sourceType);

        // optional attributes. NULL means not overlaid
        this.javaType = javaType;
        this.readable = readable;
        this.writable = writable;
        this.propertyReader = propertyReader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgAttribute resolve(AgAttribute maybeOverlaid) {
        return maybeOverlaid != null ? resolveOverlaid(maybeOverlaid) : resolveNew();
    }

    private AgAttribute resolveOverlaid(AgAttribute overlaid) {
        Class<?> javaType = this.javaType != null ? this.javaType : overlaid.getType();
        boolean readable = this.readable != null ? this.readable : overlaid.isReadable();
        boolean writable = this.writable != null ? this.writable : overlaid.isWritable();
        PropertyReader propertyReader = this.propertyReader != null ? this.propertyReader : overlaid.getPropertyReader();

        return new DefaultAgAttribute(name, javaType, readable, writable, propertyReader);
    }

    private AgAttribute resolveNew() {

        // we can't use properties from the overlaid attribute, so make sure we have all the required ones present,
        // and provide defaults where possible

        return new DefaultAgAttribute(name,
                requiredProperty("javaType", javaType),

                // using the defaults from @AgAttribute annotation
                propertyOrDefault(readable, true),
                propertyOrDefault(writable, true),

                requiredProperty("propertyReader", propertyReader));
    }
}
