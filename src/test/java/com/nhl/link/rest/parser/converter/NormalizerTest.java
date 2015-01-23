package com.nhl.link.rest.parser.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class NormalizerTest {

	@Test
	public void testNormalize_Null() {
		assertNull(Normalizer.normalize(null, "java.lang.Integer"));
		assertNull(Normalizer.normalize(null, "java.lang.Long"));
	}

	@Test
	public void testNormalize_NonNumber() {
		Object x = new Object();
		assertSame(x, Normalizer.normalize(x, "java.lang.Integer"));
		assertSame(x, Normalizer.normalize(x, "java.lang.Long"));
	}

	@Test
	public void testNormalize_Long() {
		Long smallLong = new Long(5);
		assertSame(smallLong, Normalizer.normalize(smallLong, "java.lang.Integer"));
		assertSame(smallLong, Normalizer.normalize(smallLong, "java.lang.Long"));

		Long bigLong = new Long(Integer.MAX_VALUE + 1000);
		assertSame(bigLong, Normalizer.normalize(bigLong, "java.lang.Integer"));
		assertSame(bigLong, Normalizer.normalize(bigLong, "java.lang.Long"));
	}

	@Test
	public void testNormalize_Int() {
		Integer integer = new Integer(5);
		Long smallLong = new Long(5);
		assertSame(integer, Normalizer.normalize(integer, "java.lang.Integer"));
		assertEquals(smallLong, Normalizer.normalize(integer, "java.lang.Long"));
	}
}
