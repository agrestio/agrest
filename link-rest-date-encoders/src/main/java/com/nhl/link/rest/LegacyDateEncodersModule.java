package com.nhl.link.rest;

import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.legacy.LegacyAttributeEncoderFactoryProvider;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
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

        binder.bind(IAttributeEncoderFactory.class).toProvider(LegacyAttributeEncoderFactoryProvider.class);

        binder.bindMap(Encoder.class)
                .put(java.time.LocalDateTime.class.getName(), com.nhl.link.rest.encoder.legacy.ISOLocalDateTimeEncoder.encoder())
                .put(java.time.LocalDate.class.getName(), com.nhl.link.rest.encoder.legacy.ISOLocalDateEncoder.encoder())
                .put(java.time.LocalTime.class.getName(), com.nhl.link.rest.encoder.legacy.ISOLocalTimeEncoder.encoder())
                .put(java.util.Date.class.getName(), com.nhl.link.rest.encoder.legacy.ISODateTimeEncoder.encoder())
                .put(java.sql.Timestamp.class.getName(), com.nhl.link.rest.encoder.legacy.ISODateTimeEncoder.encoder())
                .put(java.sql.Date.class.getName(), com.nhl.link.rest.encoder.legacy.ISODateEncoder.encoder())
                .put(java.sql.Time.class.getName(), com.nhl.link.rest.encoder.legacy.ISOTimeEncoder.encoder());
    }
}
