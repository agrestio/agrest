package io.agrest.meta.compiler;

import io.agrest.AgRESTException;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.it.fixture.pojo.model.P8;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class PojoEntityCompilerTest {

	private static Collection<AgEntityCompiler> compilers;

	@BeforeClass
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@Test
	public void testCompile() {
		AgEntity<Entity> entity = new PojoEntityCompiler(Collections.emptyMap())
				.compile(Entity.class, new LazyAgDataMap(compilers));
		assertNotNull(entity);
		assertEquals(1, entity.getIds().size());
		assertEquals(1, entity.getAttributes().size());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test
	public void testCompile_CollectionAttributes() {
		AgEntity<P8> entity = new PojoEntityCompiler(Collections.emptyMap())
				.compile(P8.class, new LazyAgDataMap(compilers));
		assertNotNull(entity);
		assertEquals(0, entity.getIds().size());
		assertEquals(7, entity.getAttributes().size());
		assertEquals(Collection.class, entity.getAttribute(P8.BOOLEANS).getType());
		assertEquals(Collection.class, entity.getAttribute(P8.DOUBLES).getType());
		assertEquals(Collection.class, entity.getAttribute(P8.CHARACTERS).getType());
		assertEquals(Collection.class, entity.getAttribute(P8.WILDCARD_COLLECTION).getType());
		assertEquals(Collection.class, entity.getAttribute(P8.GENERIC_COLLECTION).getType());
		assertEquals(List.class, entity.getAttribute(P8.NUMBER_LIST).getType());
		assertEquals(Set.class, entity.getAttribute(P8.STRING_SET).getType());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test
	public void testCompile_NotAnEntity() {
		AgEntity<NotAnEntity> entity = new PojoEntityCompiler(Collections.emptyMap())
				.compile(NotAnEntity.class, new LazyAgDataMap(compilers));
		assertNotNull(entity);

		try {
			entity.getAttributes();
			fail("Exception expected");
		} catch (AgRESTException e) {
			assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid entity '"));
		}
	}

	static class Entity {

		@AgId
		public String getX() {
			return "x";
		}

		@AgAttribute
		public int getY() {
			return 6;
		}
	}

	static class NotAnEntity {

		public String getX() {
			return "x";
		}

		public int getY() {
			return 6;
		}
	}
}
