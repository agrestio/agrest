package com.nhl.link.rest.client.protocol;

import com.nhl.link.rest.client.protocol.LrRequestEncoder;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.client.protocol.Sort;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ConstraintEncoderTest {

    private LrRequestEncoder encoder;

    @Before
    public void setUp() {
        encoder = LrRequestEncoder.encoder();
    }

    @Test
    public void testEncode_Sort_Ascending() throws UnsupportedEncodingException {

        Sort ordering = Sort.property("abc");
        String encoded = encoder.encode(Collections.singleton(ordering));
        assertEquals("[{\"property\":\"abc\"}]", URLDecoder.decode(encoded, "UTF-8"));
    }

    @Test
    public void testEncode_Sort_Descending() throws UnsupportedEncodingException {

        Sort ordering = Sort.property("abc").desc();
        String encoded = encoder.encode(Collections.singleton(ordering));
        assertEquals("[{\"property\":\"abc\",\"direction\":\"DESC\"}]", URLDecoder.decode(encoded, "UTF-8"));
    }

    @Test
    public void testEncode_Sort_Multiple() throws UnsupportedEncodingException {

        Sort ordering1 = Sort.property("abc").desc();
        Sort ordering2 = Sort.property("xyz");

        String encoded = encoder.encode(Arrays.asList(ordering1, ordering2));
        assertEquals("[{\"property\":\"abc\",\"direction\":\"DESC\"},{\"property\":\"xyz\"}]",
                URLDecoder.decode(encoded, "UTF-8"));
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

    @Test
    public void testEncode_Include_CayenneExpression() throws UnsupportedEncodingException {

        Include include = Include.path("abc").cayenneExp(E1.NAME.like("Jo%").andExp(E1.AGE.gt(21)));

        String encoded = encoder.encode(include);
        assertEquals("{\"path\":\"abc\",\"cayenneExp\":\"(name like 'Jo%') and (age > 21)\"}",
                URLDecoder.decode(encoded, "UTF-8"));
    }
}
