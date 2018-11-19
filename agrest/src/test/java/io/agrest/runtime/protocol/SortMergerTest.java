package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.auto._E2;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.unit.TestWithCayenneMapping;
import io.agrest.backend.query.Ordering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SortMergerTest extends TestWithCayenneMapping {

	private SortMerger merger;
	private ResourceEntity<?> entity;

	@Before
	public void before() {

		this.merger = new SortMerger(new PathDescriptorManager());

		@SuppressWarnings("unchecked")
		AgEntity<E2> age2 = mock(AgEntity.class);
		when(age2.getType()).thenReturn(E2.class);
		when(age2.getName()).thenReturn("E2");
		when(age2.getAttribute("name")).thenReturn(mock(AgAttribute.class));
		when(age2.getAttribute("address")).thenReturn(mock(AgAttribute.class));

		this.entity = new ResourceEntity<>(age2);
	}

	@Test
	public void testProcess_Array() {

        Sort sort =  new Sort(Arrays.asList(
        		new Sort("name"),
				new Sort("address")));
		merger.merge(entity, sort);

		assertEquals(2, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();

		Assert.assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
		assertEquals(_E2.ADDRESS.getName(), o2.getSortSpecString());
	}

	@Test
	public void testProcess_Simple() {

        Sort sort = new Sort("name");
		merger.merge(entity, sort);

		assertEquals(1, entity.getOrderings().size());

		Iterator<Ordering> it = entity.getOrderings().iterator();
		Ordering o1 = it.next();

		assertEquals(_E2.NAME.getName(), o1.getSortSpecString());
	}

}
