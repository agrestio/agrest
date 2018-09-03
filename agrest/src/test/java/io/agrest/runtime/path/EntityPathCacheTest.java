package io.agrest.runtime.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.agrest.LinkRestException;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.Test;

public class EntityPathCacheTest extends TestWithCayenneMapping {
	
	@Test
	public void testMultiColumnId() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E17.class));
		
		PathDescriptor pdName = cache.getPathDescriptor(new ASTObjPath("name"));
		assertNotNull(pdName);
		assertTrue(pdName.isAttribute());
		assertEquals(String.class, pdName.getType());
		assertEquals("name", pdName.getPathExp().getPath());
		assertSame(pdName, cache.getPathDescriptor(new ASTObjPath("name")));
		
		PathDescriptor pdId1 = cache.getPathDescriptor(new ASTObjPath("id1"));
		assertNotNull(pdId1);
		assertTrue(pdId1.isAttribute());
		assertEquals(Integer.class, pdId1.getType());
		assertEquals("id1", pdId1.getPathExp().getPath());
		assertSame(pdId1, cache.getPathDescriptor(new ASTObjPath("id1")));
		
		PathDescriptor pdId2 = cache.getPathDescriptor(new ASTObjPath("id2"));
		assertNotNull(pdId2);
		assertTrue(pdId2.isAttribute());
		assertEquals(Integer.class, pdId2.getType());
		assertEquals("id2", pdId2.getPathExp().getPath());
		assertSame(pdId2, cache.getPathDescriptor(new ASTObjPath("id2")));
	}

	@Test
	public void testGetPathDescriptor_Attribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E1.class));
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals(String.class, pd.getType());
		assertEquals("name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("name")));
	}

	@Test
	public void testGetPathDescriptor_Relationship() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class));
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2"));
		assertNotNull(pd);
		assertFalse(pd.isAttribute());
		assertEquals(E2.class, pd.getType());
		assertEquals("e2", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2")));
	}

	@Test
	public void testGetPathDescriptor_RelatedAttribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class));
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2.name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals(String.class, pd.getType());
		assertEquals("e2.name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2.name")));
	}

	@Test(expected = LinkRestException.class)
	public void testGetPathDescriptor_BadPath() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class));
		cache.getPathDescriptor(new ASTObjPath("e2.xyz"));
	}

	@Test
	public void testGetPathDescriptor_OuterRelatedAttribute() {
		EntityPathCache cache = new EntityPathCache(getLrEntity(E3.class));
		PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("e2+.name"));
		assertNotNull(pd);
		assertTrue(pd.isAttribute());
		assertEquals(String.class, pd.getType());
		assertEquals("e2+.name", pd.getPathExp().getPath());
		assertSame(pd, cache.getPathDescriptor(new ASTObjPath("e2+.name")));
		assertNotSame(pd, cache.getPathDescriptor(new ASTObjPath("e2.name")));
	}
}
