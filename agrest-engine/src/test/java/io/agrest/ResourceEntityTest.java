package io.agrest;

import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgEntity;
import io.agrest.pojo.model.P1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ResourceEntityTest {

	@Test
	public void testQualifiers() {
		@SuppressWarnings("unchecked")
		ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class), null);
		assertTrue(e.getQualifiers().isEmpty());

		e.getQualifiers().add(new Exp("a = 1"));
		assertEquals(1, e.getQualifiers().size());

		e.getQualifiers().add(new Exp("b = 2"));
		assertEquals(2, e.getQualifiers().size());
	}
}
