package io.agrest.base.jsonvalueconverter;

import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizerTest {

	@Test
	public void testNormalize_Null() {
		assertNull(Normalizer.normalize(null, Integer.class));
		assertNull(Normalizer.normalize(null, Long.class));
	}

	@Test
	public void testNormalize_NonNumber() {
		Object x = new Object();
		assertSame(x, Normalizer.normalize(x, Integer.class));
		assertSame(x, Normalizer.normalize(x, Long.class));
	}

	@Test
	public void testNormalize_Long() {
		Long smallLong = new Long(5);
		assertSame(smallLong, Normalizer.normalize(smallLong, Integer.class));
		assertSame(smallLong, Normalizer.normalize(smallLong, Long.class));

		Long bigLong = new Long(Integer.MAX_VALUE + 1000);
		assertSame(bigLong, Normalizer.normalize(bigLong, Integer.class));
		assertSame(bigLong, Normalizer.normalize(bigLong, Long.class));
	}

	@Test
	public void testNormalize_Int() {
		Integer integer = new Integer(5);
		Long smallLong = new Long(5);
		assertSame(integer, Normalizer.normalize(integer, Integer.class));
		assertEquals(smallLong, Normalizer.normalize(integer, Long.class));
	}
}
