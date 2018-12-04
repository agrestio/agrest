package io.agrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.agrest.encoder.EncoderFilter;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.runtime.encoder.AttributeEncoderFactoryProvider;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

public class DataResponseTest extends TestWithCayenneMapping {

	private IEncoderService encoderService;

	@Before
	public void setUp() {

		IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactoryProvider(Collections.emptyMap()).get();
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
		this.encoderService = new EncoderService(Collections.<EncoderFilter> emptyList(), attributeEncoderFactory,
				stringConverterFactory, new RelationshipMapper(),
				Collections.<String, PropertyMetadataEncoder> emptyMap(),
				expressionConverter, expressionMatcher, orderingConverter, orderingSorter);
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
