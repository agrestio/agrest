package io.agrest.meta;

import io.agrest.reader.DataReader;

/**
 * @since 5.0
 */
public class DefaultAttributeOverlay extends BasePropertyOverlay implements AgAttributeOverlay {

    private final Class<?> javaType;
    private final Boolean readable;
    private final Boolean writable;
    private final DataReader dataReader;

    public DefaultAttributeOverlay(
            String name,
            Class<?> sourceType,
            Class<?> javaType,
            Boolean readable,
            Boolean writable,
            DataReader dataReader) {

        super(name, sourceType);

        // optional attributes. NULL means not overlaid
        this.javaType = javaType;
        this.readable = readable;
        this.writable = writable;
        this.dataReader = dataReader;
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
        DataReader dataReader = this.dataReader != null ? this.dataReader : overlaid.getDataReader();

        return new DefaultAttribute(name, javaType, readable, writable, dataReader);
    }

    private AgAttribute resolveNew() {

        // we can't use properties from the overlaid attribute, so make sure we have all the required ones present,
        // and provide defaults where possible

        return new DefaultAttribute(name,
                requiredProperty("javaType", javaType),

                // using the defaults from @AgAttribute annotation
                propertyOrDefault(readable, true),
                propertyOrDefault(writable, true),

                requiredProperty("dataReader", dataReader));
    }
}
