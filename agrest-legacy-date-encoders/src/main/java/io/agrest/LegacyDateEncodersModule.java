package io.agrest;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.encoder.legacy.ISODateConverter;
import io.agrest.encoder.legacy.ISODateTimeConverter;
import io.agrest.encoder.legacy.ISOTimeConverter;
import io.agrest.encoder.legacy.LegacyAttributeEncoderFactoryProvider;
import io.agrest.encoder.legacy.LegacyStringConverterFactoryProvider;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * Provides old date encoders, that have been used prior to Link Rest 2.11
 *
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyDateEncodersModule implements Module {

    @Override
    public void configure(Binder binder) {

        binder.bind(IStringConverterFactory.class).toProvider(LegacyStringConverterFactoryProvider.class);

        binder.bindMap(StringConverter.class)
                .put(java.util.Date.class.getName(), ISODateTimeConverter.converter())
                .put(java.sql.Timestamp.class.getName(), ISODateTimeConverter.converter())
                .put(java.sql.Date.class.getName(), ISODateConverter.converter())
                .put(java.sql.Time.class.getName(), ISOTimeConverter.converter());

        binder.bind(IAttributeEncoderFactory.class).toProvider(LegacyAttributeEncoderFactoryProvider.class);

        binder.bindMap(Encoder.class)
                .put(java.time.LocalDateTime.class.getName(), io.agrest.encoder.legacy.ISOLocalDateTimeEncoder.encoder())
                .put(java.time.LocalDate.class.getName(), io.agrest.encoder.legacy.ISOLocalDateEncoder.encoder())
                .put(java.time.LocalTime.class.getName(), io.agrest.encoder.legacy.ISOLocalTimeEncoder.encoder())
                .put(java.util.Date.class.getName(), io.agrest.encoder.legacy.ISODateTimeEncoder.encoder())
                .put(java.sql.Timestamp.class.getName(), io.agrest.encoder.legacy.ISODateTimeEncoder.encoder())
                .put(java.sql.Date.class.getName(), io.agrest.encoder.legacy.ISODateEncoder.encoder())
                .put(java.sql.Time.class.getName(), io.agrest.encoder.legacy.ISOTimeEncoder.encoder());
    }
}
