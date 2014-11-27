package com.nhl.link.rest.runtime.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.pojo.model.P3;
import com.nhl.link.rest.it.fixture.pojo.model.P4;
import com.nhl.link.rest.it.fixture.pojo.model.P5;
import com.nhl.link.rest.runtime.meta.ObjEntityBuilder;
import com.nhl.link.rest.runtime.meta.RootDataMapBuilder;

public class ObjEntityBuilderTest {

	@Test
	public void testToEntity_Default() {

		RootDataMapBuilder parent = new RootDataMapBuilder("xmap");
		DataMap dataMap = parent.getMap();
		ObjEntityBuilder builder = new ObjEntityBuilder(parent, P3.class);

		ObjEntity p3e = builder.toEntity();
		assertNotNull(p3e);
		assertEquals("P3", p3e.getName());
		assertSame(p3e, dataMap.getObjEntity("P3"));

		assertEquals(1, p3e.getAttributes().size());
		ObjAttribute name = p3e.getAttribute("name");
		assertNotNull(name);
		assertEquals("name", name.getName());
		assertEquals("java.lang.String", name.getType());
	}

	@Test
	public void testToOneRelationship() {

		RootDataMapBuilder parent = new RootDataMapBuilder("xmap");
		DataMap dataMap = parent.getMap();
		ObjEntityBuilder builder = new ObjEntityBuilder(parent, P4.class);

		ObjEntity p4e = builder.toEntity();
		assertNotNull(p4e);
		assertEquals("P4", p4e.getName());
		assertSame(p4e, dataMap.getObjEntity("P4"));

		assertEquals(0, p4e.getAttributes().size());
		assertEquals(1, p4e.getRelationships().size());

		ObjRelationship p3r = p4e.getRelationship("p3");
		assertNotNull(p3r);
		assertEquals("P3", p3r.getTargetEntityName());
		assertFalse(p3r.isToMany());
	}

	@Test
	public void testToManyRelationship() {

		RootDataMapBuilder parent = new RootDataMapBuilder("xmap");
		DataMap dataMap = parent.getMap();
		ObjEntityBuilder builder = new ObjEntityBuilder(parent, P5.class);

		ObjEntity p5e = builder.toEntity();
		assertNotNull(p5e);
		assertEquals("P5", p5e.getName());
		assertSame(p5e, dataMap.getObjEntity("P5"));

		assertEquals(0, p5e.getAttributes().size());
		assertEquals(1, p5e.getRelationships().size());

		ObjRelationship p4sr = p5e.getRelationship("p4s");
		assertNotNull(p4sr);
		assertEquals("P4", p4sr.getTargetEntityName());
		assertTrue(p4sr.isToMany());
	}

}
