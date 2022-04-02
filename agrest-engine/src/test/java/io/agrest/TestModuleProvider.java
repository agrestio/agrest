package io.agrest;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

import java.util.Collection;
import java.util.Collections;

public class TestModuleProvider implements AgModuleProvider {

    public static final String CONVERTER_KEY = X.class.getName();

    @Override
    public Module module() {
        return new TestModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return TestModule.class;
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.emptyList();
    }

    public static class TestModule implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bindMap(JsonValueConverter.class).put(CONVERTER_KEY, new XConverter());
        }
    }

    public static class X {
    }

    public static class XConverter implements JsonValueConverter<X> {
        @Override
        public X value(JsonNode node) {
            return null;
        }
    }
}
