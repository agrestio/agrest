package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.encoder.converter.StringConverter;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.runtime.encoder.StringConverterFactory;

import java.sql.Types;
import java.util.Map;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyStringConverterFactory extends StringConverterFactory {

    public LegacyStringConverterFactory(Map<Class<?>, StringConverter> knownConverters,
                                        StringConverter defaultConverter) {
        super(knownConverters, defaultConverter);
    }

    @Override
    protected StringConverter buildConverter(LrAttribute attribute) {

        if (java.util.Date.class.equals(attribute.getType())
                && attribute instanceof LrPersistentAttribute) {

            int jdbcType = ((LrPersistentAttribute) attribute).getJdbcType();

            if (jdbcType == Types.DATE) {
                return ISODateConverter.converter();
            } else if (jdbcType == Types.TIME) {
                return ISOTimeConverter.converter();
            }
			// JDBC TIMESTAMP or something entirely unrecognized
		}
        return super.buildConverter(attribute);
    }
}
