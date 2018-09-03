package io.agrest.encoder.legacy;

import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.runtime.encoder.StringConverterFactory;

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
    protected StringConverter buildConverter(AgAttribute attribute) {

        if (java.util.Date.class.equals(attribute.getType())
                && attribute instanceof AgPersistentAttribute) {

            int jdbcType = ((AgPersistentAttribute) attribute).getJdbcType();

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
