package io.agrest.compiler;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.LazySchema;
import io.agrest.pojo.model.P3;
import io.agrest.pojo.model.P4;
import io.agrest.pojo.model.P5;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class AnnotationsAgEntityBuilderTest {

	private static Collection<AgEntityCompiler> COMPILERS;

	@BeforeAll
	public static void setUpClass() {
		COMPILERS = asList(new AnnotationsAgEntityCompiler(Collections.emptyMap()));
	}

	@Test
	public void testBuild_Default() {

		AgEntity<P3> p3e = new AnnotationsAgEntityBuilder<>(P3.class, new LazySchema(COMPILERS)).build();
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

		AgEntity<P4> p4e = new AnnotationsAgEntityBuilder<>(P4.class, new LazySchema(COMPILERS)).build();
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

		AgEntity<P5> p5e = new AnnotationsAgEntityBuilder<>(P5.class, new LazySchema(COMPILERS)).build();

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
