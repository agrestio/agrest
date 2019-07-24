package io.agrest;

import io.agrest.it.fixture.cayenne.E1;
import io.agrest.runtime.encoder.AttributeEncoderFactory;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.encoder.ValueEncodersProvider;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class DataResponseTest extends TestWithCayenneMapping {

	private IEncoderService encoderService;

	@Before
	public void setUp() {

		IAttributeEncoderFactory aef = new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get());
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
		this.encoderService = new EncoderService(Collections.emptyList(), aef,
				stringConverterFactory, new RelationshipMapper(),
				Collections.emptyMap());
	}

	@Test
	public void testToResponse_PlainObjects() {

		ResourceEntity<E1> resourceEntity = getResourceEntity(E1.class);

		List<E1> o1 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			o1.add(new E1());
		}

		DataResponse<E1> response = DataResponse.forType(E1.class);
		response.setObjects(o1);
		response.setEncoder(encoderService.dataEncoder(resourceEntity));

		assertNotNull(response);
		assertEquals(o1, response.getObjects());
	}
}
