package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;

import java.sql.Types;
import java.util.Map;

public class LegacyAttributeEncoderFactory extends AttributeEncoderFactory {

    public LegacyAttributeEncoderFactory(Map<Class<?>, Encoder> knownEncoders, Encoder defaultEncoder) {
        super(knownEncoders, defaultEncoder);
    }

    @Override
    protected Encoder buildEncoder(LrAttribute attribute) {

        if (java.util.Date.class.equals(attribute.getType())
                && attribute instanceof LrPersistentAttribute) {

            int jdbcType = ((LrPersistentAttribute) attribute).getJdbcType();

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
