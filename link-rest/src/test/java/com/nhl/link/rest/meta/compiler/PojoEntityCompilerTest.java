package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PojoEntityCompilerTest {

	private static Collection<LrEntityCompiler> compilers;
	private static IJsonValueConverterFactory converterFactory;

	@BeforeClass
	public static void setUpClass() {
		converterFactory = new DefaultJsonValueConverterFactory();
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(converterFactory));
	}

	@Test
	public void testCompile() {
		LrEntity<Entity> entity = new PojoEntityCompiler(converterFactory)
				.compile(Entity.class, new LazyLrDataMap(compilers));
		assertNotNull(entity);
		assertEquals(1, entity.getIds().size());
		assertEquals(1, entity.getAttributes().size());
		assertEquals(0, entity.getRelationships().size());
	}

	@Test(expected = Exception.class)
	public void testCompile_NotAnEntity() {
		LrEntity<NotAnEntity> entity = new PojoEntityCompiler(converterFactory)
				.compile(NotAnEntity.class, new LazyLrDataMap(compilers));
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
