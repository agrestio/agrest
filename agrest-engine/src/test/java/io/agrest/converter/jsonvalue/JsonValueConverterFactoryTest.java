package io.agrest.converter.jsonvalue;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class JsonValueConverterFactoryTest {

    @Test
    public void testConverter() {

        JsonValueConverter<?> c1 = mock(JsonValueConverter.class);
        JsonValueConverter<?> c2 = mock(JsonValueConverter.class);

        JsonValueConverterFactory factory =
                new JsonValueConverterFactory(Collections.singletonMap(Long.class, c1), c2);

        assertSame(c1, factory.converter(Long.class));
        assertSame(c2, factory.converter(Long.TYPE));

        assertSame(c2, factory.converter(this.getClass()));
        assertSame(c2, factory.converter(Object.class));
    }

    @Test
    public void testConverter_Enum() {

        JsonValueConverter<?> c1 = mock(JsonValueConverter.class);
        JsonValueConverter<?> c2 = mock(JsonValueConverter.class);

        JsonValueConverterFactory factory =
                new JsonValueConverterFactory(Collections.emptyMap(), c2);

        assertSame(c2, factory.converter(Object.class));

        JsonValueConverter<?> e1c  = factory.converter(E1.class);
        assertTrue(e1c instanceof EnumConverter);
        assertSame(E1.class, ((EnumConverter) e1c).getEnumType());

        JsonValueConverter<?> e2c  = factory.converter(E2.class);
        assertTrue(e2c instanceof EnumConverter);
        assertSame(E2.class, ((EnumConverter) e2c).getEnumType());

        assertSame(e1c, factory.converter(E1.class));
    }

    public enum E1 {
        e11, e12
    }

    public enum E2 {
        e21, e22
    }
}
