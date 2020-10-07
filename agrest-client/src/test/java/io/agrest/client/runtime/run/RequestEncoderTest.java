package io.agrest.client.runtime.run;

import io.agrest.client.protocol.Expression;
import io.agrest.client.protocol.Include;
import io.agrest.client.protocol.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestEncoderTest {

	private RequestEncoder encoder;

	@BeforeEach
	public void setUp() {
		encoder = RequestEncoder.encoder();
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

		Include include = Include.path("abc").build();
		String encoded = encoder.encode(include);
		assertEquals(include.getPath(), encoded);
	}

	@Test
	public void testEncode_Include_Constrained() throws UnsupportedEncodingException {

		Include include = Include.path("abc").mapBy("related").start(50).limit(100).sort("s1", "s2")
				.sort(Sort.property("d1").desc()).build();

		String encoded = encoder.encode(include);
		assertEquals(
				"{\"path\":\"abc\",\"mapBy\":\"related\",\"start\":50,\"limit\":100,"
						+ "\"sort\":[{\"property\":\"d1\",\"direction\":\"DESC\"},{\"property\":\"s1\"},{\"property\":\"s2\"}]}",
				URLDecoder.decode(encoded, "UTF-8"));
	}

	@Test
	public void testEncode_Include_CayenneExpression() throws UnsupportedEncodingException {

		Include include = Include.path("abc")
				.cayenneExp(Expression.query("name like $name and age >= $age").param("name", "Jo%").param("age", 21))
				.build();

		String encoded = encoder.encode(include);
		assertEquals("{\"path\":\"abc\",\"cayenneExp\":{\"exp\":\"name like $name and age >= $age\"," +
				"\"params\":{\"name\":\"Jo%\",\"age\":\"21\"}}}",
				URLDecoder.decode(encoded, "UTF-8"));
	}
}
