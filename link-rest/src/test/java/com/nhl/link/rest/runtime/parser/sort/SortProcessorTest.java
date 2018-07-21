package com.nhl.link.rest.runtime.parser.sort;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import com.nhl.link.rest.protocol.Sort;
import org.apache.cayenne.query.Ordering;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.auto._E2;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class SortProcessorTest extends TestWithCayenneMapping {

	private SortParser parser;
	private SortConstructor constructor;
	private ResourceEntity<?> entity;

	@Before
	public void before() {
		JacksonService jacksonService = new JacksonService();
		this.parser = new SortParser(jacksonService);
		this.constructor = new SortConstructor(new PathCache());

		@SuppressWarnings("unchecked")
		LrEntity<E2> lre2 = mock(LrEntity.class);
		when(lre2.getType()).thenReturn(E2.class);
		when(lre2.getName()).thenReturn("E2");
		when(lre2.getAttribute("name")).thenReturn(mock(LrAttribute.class));
		when(lre2.getAttribute("address")).thenReturn(mock(LrAttribute.class));

		this.entity = new ResourceEntity<>(lre2);
	}

	@Test
	public void testProcess_Array() {

        Sort sort = parser.fromString("[{\"property\":\"name\"},{\"property\":\"address\"}]", null);
	    constructor.construct(entity, sort);

		assertEquals(2, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
		assertEquals(_E2.ADDRESS.getName(), o2.getSortSpecString());
	}

	@Test
	public void testProcess_Object() {

        Sort sort = parser.fromString("{\"property\":\"name\"}", null);
        constructor.construct(entity, sort);

		assertEquals(1, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testProcess_Simple() {

        Sort sort = parser.fromString("name", null);
        constructor.construct(entity, sort);

		assertEquals(1, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
	}

}
