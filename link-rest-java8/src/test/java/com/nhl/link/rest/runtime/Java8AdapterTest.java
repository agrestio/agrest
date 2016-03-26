package com.nhl.link.rest.runtime;

import com.nhl.link.rest.runtime.adapter.java8.Java8Adapter;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.Java8AttributeEncoderFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.Java8JsonValueConverterFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Java8AdapterTest {

    @Test
    public void testBuild_Adapter() {
        Java8Adapter adapter = new Java8Adapter();
        LinkRestRuntime runtime = new LinkRestBuilder().adapter(adapter).build();

        assertEquals(Java8JsonValueConverterFactory.class, runtime.service(IJsonValueConverterFactory.class).getClass());
        assertEquals(Java8AttributeEncoderFactory.class, runtime.service(IAttributeEncoderFactory.class).getClass());
    }

}
