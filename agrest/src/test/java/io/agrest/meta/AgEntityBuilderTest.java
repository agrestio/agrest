package io.agrest.meta;

import io.agrest.it.fixture.pojo.model.P3;
import io.agrest.it.fixture.pojo.model.P4;
import io.agrest.it.fixture.pojo.model.P5;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class AgEntityBuilderTest {

	private static Collection<AgEntityCompiler> COMPILERS;

	@BeforeClass
	public static void setUpClass() {
		COMPILERS = asList(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@Test
	public void testBuild_Default() {

		AgEntity<P3> p3e = new AgEntityBuilder<>(P3.class, new LazyAgDataMap(COMPILERS)).build();
		assertNotNull(p3e);
		assertEquals("P3", p3e.getName());

		assertEquals(0, p3e.getRelationships().size());
		assertEquals(1, p3e.getAttributes().size());

		AgAttribute name = p3e.getAttribute("name");
		assertNotNull(name);
		assertEquals("name", name.getName());
		assertEquals(String.class, name.getType());
	}

	@Test
	public void testToOneRelationship() {

		AgEntity<P4> p4e = new AgEntityBuilder<>(P4.class, new LazyAgDataMap(COMPILERS)).build();
		assertNotNull(p4e);
		assertEquals("P4", p4e.getName());

		assertEquals(1, p4e.getRelationships().size());
		assertEquals(0, p4e.getAttributes().size());

		AgRelationship p3r = p4e.getRelationship("p3");
		assertNotNull(p3r);
		assertEquals(P3.class, p3r.getTargetEntity().getType());
		assertFalse(p3r.isToMany());
	}

	@Test
	public void testToManyRelationship() {

		AgEntity<P5> p5e = new AgEntityBuilder<>(P5.class, new LazyAgDataMap(COMPILERS)).build();

		assertNotNull(p5e);
		assertEquals("P5", p5e.getName());

		assertEquals(1, p5e.getRelationships().size());
		assertEquals(0, p5e.getAttributes().size());

		AgRelationship p4sr = p5e.getRelationship("p4s");
		assertNotNull(p4sr);
		assertEquals(P4.class, p4sr.getTargetEntity().getType());
		assertTrue(p4sr.isToMany());
	}

}
