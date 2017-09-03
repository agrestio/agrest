package com.nhl.link.rest.meta;

import com.nhl.link.rest.it.fixture.pojo.model.P3;
import com.nhl.link.rest.it.fixture.pojo.model.P4;
import com.nhl.link.rest.it.fixture.pojo.model.P5;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.meta.compiler.PojoEntityCompiler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class LrjEntityBuilderTest {

	private static Collection<LrEntityCompiler> COMPILERS;

	@BeforeClass
	public static void setUpClass() {
		COMPILERS = asList(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@Test
	public void testBuild_Default() {

		LrEntity<P3> p3e = new LrEntityBuilder<>(P3.class, new LazyLrDataMap(COMPILERS)).build();
		assertNotNull(p3e);
		assertEquals("P3", p3e.getName());

		assertEquals(0, p3e.getRelationships().size());
		assertEquals(1, p3e.getAttributes().size());

		LrAttribute name = p3e.getAttribute("name");
		assertNotNull(name);
		assertEquals("name", name.getName());
		assertEquals(String.class, name.getType());
	}

	@Test
	public void testToOneRelationship() {

		LrEntity<P4> p4e = new LrEntityBuilder<>(P4.class, new LazyLrDataMap(COMPILERS)).build();
		assertNotNull(p4e);
		assertEquals("P4", p4e.getName());

		assertEquals(1, p4e.getRelationships().size());
		assertEquals(0, p4e.getAttributes().size());

		LrRelationship p3r = p4e.getRelationship("p3");
		assertNotNull(p3r);
		assertEquals(P3.class, p3r.getTargetEntity().getType());
		assertFalse(p3r.isToMany());
	}

	@Test
	public void testToManyRelationship() {

		LrEntity<P5> p5e = new LrEntityBuilder<>(P5.class, new LazyLrDataMap(COMPILERS)).build();

		assertNotNull(p5e);
		assertEquals("P5", p5e.getName());

		assertEquals(1, p5e.getRelationships().size());
		assertEquals(0, p5e.getAttributes().size());

		LrRelationship p4sr = p5e.getRelationship("p4s");
		assertNotNull(p4sr);
		assertEquals(P4.class, p4sr.getTargetEntity().getType());
		assertTrue(p4sr.isToMany());
	}

}
