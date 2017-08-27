package com.nhl.link.rest.parser.converter;

import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactory;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class DefaultJsonValueConverterFactoryTest {

    @Test
    public void testConverter() {

        DefaultJsonValueConverterFactory factory = new DefaultJsonValueConverterFactory();

        assertSame(LongConverter.converter(), factory.converter(Long.class));
        assertSame(LongConverter.converter(), factory.converter(Long.TYPE));

        assertSame(GenericConverter.converter(), factory.converter(this.getClass()));
        assertSame(GenericConverter.converter(), factory.converter(Object.class));
    }
}
