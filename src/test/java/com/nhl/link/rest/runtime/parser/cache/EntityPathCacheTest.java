package com.nhl.link.rest.runtime.parser.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.Test;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class EntityPathCacheTest extends TestWithCayenneMapping {

	@Test
	public void testGetPathDescriptor_Attribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E1.class), metadataService);
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals("java.lang.String", pd.getType());
		assertEquals("name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("name")));
	}

	@Test
	public void testGetPathDescriptor_Relationship() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class), metadataService);
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2"));
		assertNotNull(pd);
		assertFalse(pd.isAttribute());
		assertEquals(E2.class.getName(), pd.getType());
		assertEquals("e2", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2")));
	}

	@Test
	public void testGetPathDescriptor_RelatedAttribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class), metadataService);
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2.name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals("java.lang.String", pd.getType());
		assertEquals("e2.name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2.name")));
	}

	@Test(expected = LinkRestException.class)
	public void testGetPathDescriptor_BadPath() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class), metadataService);
		cache.getPathDescriptor(new ASTObjPath("e2.xyz"));
	}

	@Test
	public void testGetPathDescriptor_OuterRelatedAttribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class), metadataService);
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2+.name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals("java.lang.String", pd.getType());
		assertEquals("e2+.name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2+.name")));
		assertNotSame(pd, cache.getPathDescriptor(new ASTObjPath("e2.name")));
	}
}
