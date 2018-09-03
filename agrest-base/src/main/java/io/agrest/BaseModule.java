package io.agrest;

import io.agrest.parser.converter.JsonValueConverter;
import io.agrest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import io.agrest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

public class BaseModule implements Module {

    @Override
    public void configure(Binder binder) {
        // a map of custom converters
        binder.bindMap(JsonValueConverter.class);
        binder.bind(IJsonValueConverterFactory.class).toProvider(DefaultJsonValueConverterFactoryProvider.class);
    }
}
