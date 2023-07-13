package io.agrest.converter.jsonvalue;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class JsonValueConvertersProviderTest {

    public JsonValueConverters createConverters(Map<String, JsonValueConverter<?>> injected) {
        return new JsonValueConvertersProvider(injected).get();
    }

    @Test
    public void converter_Injected() {
        JsonValueConverter<?> customConverter = mock(JsonValueConverter.class);
        Map<String, JsonValueConverter<?>> injected = Map.of(CustomType.class.getName(), customConverter);
        assertSame(customConverter, createConverters(injected).converter(CustomType.class));
    }

    public static class CustomType {

    }
}
