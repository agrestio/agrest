package io.agrest.base;

import io.agrest.base.jsonvalueconverter.JsonValueConverter;
import io.agrest.base.jsonvalueconverter.DefaultJsonValueConverterFactoryProvider;
import io.agrest.base.jsonvalueconverter.IJsonValueConverterFactory;
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
