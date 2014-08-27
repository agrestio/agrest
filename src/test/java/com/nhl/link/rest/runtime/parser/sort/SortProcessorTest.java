package com.nhl.link.rest.runtime.parser.sort;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.Ordering;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.sort.SortWorker;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.auto._E2;

public class SortProcessorTest extends TestWithCayenneMapping {

	private SortWorker processor;
	private Entity<?> entity;

	@Before
	public void before() {
		JacksonService jacksonService = new JacksonService();
		this.processor = new SortWorker(jacksonService, new PathCache());

		ObjEntity oEntity = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);
		this.entity = new Entity<>(Object.class, oEntity);
	}

	@Test
	public void testProcess_Array() {

		processor.process(entity, "[{\"property\":\"name\"},{\"property\":\"address\"}]", null);

		assertEquals(2, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
		assertEquals(_E2.ADDRESS.getName(), o2.getSortSpecString());
	}

	@Test
	public void testProcess_Object() {

		processor.process(entity, "{\"property\":\"name\"}", null);

		assertEquals(1, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testProcess_Simple() {

		processor.process(entity, "name", null);

		assertEquals(1, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
	}

}
