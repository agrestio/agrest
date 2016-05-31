package com.nhl.link.rest.client;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ConstraintEncoderTest {

    private ConstraintEncoder encoder;

    @Before
    public void setUp() {
        encoder = ConstraintEncoder.encoder();
    }

    @Test
    public void testEncode_Sort_Ascending() {

        Sort ordering = Sort.property("abc");
        String encoded = encoder.encode(Collections.singleton(ordering));
        assertEquals("{\"property\":\"abc\"}", encoded);
    }

    @Test
    public void testEncode_Sort_Descending() {

        Sort ordering = Sort.property("abc").desc();
        String encoded = encoder.encode(Collections.singleton(ordering));
        assertEquals("{\"property\":\"abc\",\"direction\":\"DESC\"}", encoded);
    }

    @Test
    public void testEncode_Sort_Multiple() {

        Sort ordering1 = Sort.property("abc").desc();
        Sort ordering2 = Sort.property("xyz");

        String encoded = encoder.encode(Arrays.asList(ordering1, ordering2));
        assertEquals("[{\"property\":\"abc\",\"direction\":\"DESC\"},{\"property\":\"xyz\"}]", encoded);
    }

    @Test
    public void testEncode_Include_Simple() {

        Include include = Include.path("abc");
        String encoded = encoder.encode(include);
        assertEquals(include.getPath(), encoded);
    }

    @Test
    public void testEncode_Include_Constrained() throws UnsupportedEncodingException {

        Include include = Include.path("abc").mapBy("related").start(50).limit(100)
                .sort("s1", "s2").sort(Sort.property("d1").desc());

        String encoded = encoder.encode(include);
        assertEquals("{\"path\":\"abc\",\"mapBy\":\"related\",\"start\":50,\"limit\":100," +
                "\"sort\":[{\"property\":\"d1\",\"direction\":\"DESC\"},{\"property\":\"s1\"},{\"property\":\"s2\"}]}",
                URLDecoder.decode(encoded, "UTF-8"));
    }
}
