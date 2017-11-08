package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.encoder.converter.StringConverter;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.encoder.StringConverterFactoryProvider;
import org.apache.cayenne.di.Inject;

import java.util.Map;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyStringConverterFactoryProvider extends StringConverterFactoryProvider {

    public LegacyStringConverterFactoryProvider(@Inject Map<String, StringConverter> injectedConverters) {
        super(injectedConverters);
    }

    @Override
    protected IStringConverterFactory createFactory(Map<Class<?>, StringConverter> converters, StringConverter defaultConverter) {
        return new LegacyStringConverterFactory(converters, defaultConverter);
    }
}
