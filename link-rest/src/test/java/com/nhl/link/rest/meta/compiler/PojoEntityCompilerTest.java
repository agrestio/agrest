package com.nhl.link.rest.meta.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrEntity;

public class PojoEntityCompilerTest {

	private static Collection<LrEntityCompiler> compilers;

	@BeforeClass
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler());
	}

	@Test
	public void testCompile() {
		LrEntity<Entity> entity = new PojoEntityCompiler().compile(Entity.class, new LazyLrDataMap(compilers));
		assertNotNull(entity);
		assertEquals(1, entity.getIds().size());
		assertEquals(1, entity.getAttributes().size());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test(expected = Exception.class)
	public void testCompile_NotAnEntity() {
		LrEntity<NotAnEntity> entity = new PojoEntityCompiler().compile(NotAnEntity.class, new LazyLrDataMap(compilers));
		assertNotNull(entity);

		try {
			entity.getAttributes();
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith("Not an entity"));
			throw e;
		}
	}

	static class Entity {

		@LrId
		public String getX() {
			return "x";
		}

		@LrAttribute
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
