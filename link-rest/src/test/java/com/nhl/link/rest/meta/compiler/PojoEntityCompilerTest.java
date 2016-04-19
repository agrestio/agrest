package com.nhl.link.rest.meta.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.meta.LrEntity;

public class PojoEntityCompilerTest {

	@Test
	public void testCompile() {
		LrEntity<Entity> entity = new PojoEntityCompiler().compile(Entity.class);
		assertNotNull(entity);
		assertEquals(1, entity.getIds().size());
		assertEquals(1, entity.getAttributes().size());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test
	public void testCompileSkip() {
		LrEntity<NotAnEntity> entity = new PojoEntityCompiler().compile(NotAnEntity.class);
		assertNull(entity);
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
