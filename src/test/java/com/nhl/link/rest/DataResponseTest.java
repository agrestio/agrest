package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E1;

public class DataResponseTest extends TestWithCayenneMapping {

	private IEncoderService encoderService;

	@Before
	public void setUp() {

		IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactory();
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
		this.encoderService = new EncoderService(Collections.<EncoderFilter> emptyList(), attributeEncoderFactory,
				stringConverterFactory, new RelationshipMapper());
	}

	@Test
	public void testToResponse_PlainObjects() {
		DataResponse<E1> request = DataResponse.forType(E1.class).withClientEntity(getClientEntity(E1.class));

		List<E1> o1 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			o1.add(new E1());
		}

		DataResponse<E1> r1 = encoderService.makeEncoder(request).withObjects(o1);

		assertNotNull(r1);
		assertEquals(o1, r1.getObjects());
	}
}
