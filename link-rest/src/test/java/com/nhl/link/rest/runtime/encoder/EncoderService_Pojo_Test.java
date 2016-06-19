package com.nhl.link.rest.runtime.encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P6;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LazyLrDataMap;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.meta.compiler.PojoEntityCompiler;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderService_Pojo_Test {

	private static Collection<LrEntityCompiler> compilers;

	private EncoderService encoderService;
	private List<EncoderFilter> filters;

	@BeforeClass
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler());
	}

	@Before
	public void setUp() {

		this.filters = new ArrayList<>();

		IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactory();
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

		this.encoderService = new EncoderService(this.filters, attributeEncoderFactory, stringConverterFactory,
				new RelationshipMapper(), Collections.<String, PropertyMetadataEncoder> emptyMap());
	}

	@Test
	public void testEncode_SimplePojo_noId() throws IOException {
		LrEntity<P1> p1lre = LrEntityBuilder.build(P1.class, new LazyLrDataMap(compilers));
		ResourceEntity<P1> descriptor = new ResourceEntity<P1>(p1lre);
		descriptor.getAttributes().put("name", new DefaultLrAttribute("name", String.class));

		P1 p1 = new P1();
		p1.setName("XYZ");
		assertEquals("{\"data\":[{\"name\":\"XYZ\"}],\"total\":1}", toJson(p1, descriptor));
	}

	@Test
	public void testEncode_SimplePojo_Id() throws IOException {

		P6 p6 = new P6();
		p6.setStringId("myid");
		p6.setIntProp(4);

		LrEntity<P6> p6lre = LrEntityBuilder.builder(P6.class, new LazyLrDataMap(compilers)).build();
		ResourceEntity<P6> descriptor = new ResourceEntity<P6>(p6lre);
		descriptor.getAttributes().put("intProp", new DefaultLrAttribute("intProp", Integer.class));
		descriptor.includeId();

		assertEquals("{\"data\":[{\"id\":\"myid\",\"intProp\":4}],\"total\":1}", toJson(p6, descriptor));
	}

	private String toJson(Object object, ResourceEntity<?> resourceEntity) throws IOException {

		Encoder encoder = encoderService.dataEncoder(resourceEntity);

		// wrap in collection... root encoder expects a list...
		object = Collections.singletonList(object);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try (JsonGenerator generator = new JacksonService().getJsonFactory().createGenerator(out, JsonEncoding.UTF8)) {
			encoder.encode(null, object, generator);
		}

		return new String(out.toByteArray(), "UTF-8");
	}

}
