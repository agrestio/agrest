package io.agrest.encoder.legacy;

import io.agrest.encoder.Encoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.runtime.encoder.AttributeEncoderFactory;

import java.sql.Types;
import java.util.Map;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyAttributeEncoderFactory extends AttributeEncoderFactory {

    public LegacyAttributeEncoderFactory(Map<Class<?>, Encoder> knownEncoders, Encoder defaultEncoder) {
        super(knownEncoders, defaultEncoder);
    }

    @Override
    protected Encoder buildEncoder(AgAttribute attribute) {

        if (java.util.Date.class.equals(attribute.getType())
                && attribute instanceof AgPersistentAttribute) {

            int jdbcType = ((AgPersistentAttribute) attribute).getJdbcType();

            if (jdbcType == Types.DATE) {
                return ISODateEncoder.encoder();
            } else if (jdbcType == Types.TIME) {
                return ISOTimeEncoder.encoder();
            }
            // JDBC TIMESTAMP or something entirely unrecognized
        }
        return super.buildEncoder(attribute);
    }
}
