package com.nhl.link.rest.parser.converter;

import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class DefaultJsonValueConverterFactoryProviderTest {

    @Test
    public void testGet_KnownConverters() {

        IJsonValueConverterFactory factory = new DefaultJsonValueConverterFactoryProvider().get();

        assertSame(LongConverter.converter(), factory.converter(Long.class));
        assertSame(LongConverter.converter(), factory.converter(Long.TYPE));

        assertSame(GenericConverter.converter(), factory.converter(this.getClass()));
        assertSame(GenericConverter.converter(), factory.converter(Object.class));
    }
}
