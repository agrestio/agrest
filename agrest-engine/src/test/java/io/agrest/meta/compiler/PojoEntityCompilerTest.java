package io.agrest.meta.compiler;

import io.agrest.AgException;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.pojo.model.P8;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PojoEntityCompilerTest {

	private static Collection<AgEntityCompiler> compilers;

	@BeforeAll
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@Test
	public void testCompile() {
		AgEntity<Entity> entity = new PojoEntityCompiler(Collections.emptyMap())
				.compile(Entity.class, new LazyAgDataMap(compilers));
		assertNotNull(entity);
		assertEquals(1, entity.getIdParts().size());
		assertEquals(1, entity.getAttributes().size());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test
	public void testCompile_CollectionAttributes() {
		AgEntity<P8> entity = new PojoEntityCompiler(Collections.emptyMap())
				.compile(P8.class, new LazyAgDataMap(compilers));
		assertNotNull(entity);
		assertEquals(0, entity.getIdParts().size());
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
		} catch (AgException e) {
			assertTrue(e.getMessage().startsWith("Invalid entity '"), e.getMessage());
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
