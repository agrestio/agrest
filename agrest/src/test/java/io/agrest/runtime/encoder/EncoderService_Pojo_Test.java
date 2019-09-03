package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.Encoders;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.it.fixture.pojo.model.P6;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.semantics.RelationshipMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EncoderService_Pojo_Test {

	private static Collection<AgEntityCompiler> compilers;

	private EncoderService encoderService;

	@BeforeClass
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@Before
	public void setUp() {

		IAttributeEncoderFactory aef = new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get());
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

		this.encoderService = new EncoderService(
				aef,
				stringConverterFactory,
				new RelationshipMapper(),
				Collections.emptyMap());
	}

	@Test
	public void testEncode_SimplePojo_noId() {
		AgEntity<P1> p1age = new AgEntityBuilder<>(P1.class, new LazyAgDataMap(compilers)).build();
		ResourceEntity<P1> descriptor = new ResourceEntity<P1>(p1age);
		descriptor.getAttributes().put("name", new DefaultAgAttribute("name", String.class));

		P1 p1 = new P1();
		p1.setName("XYZ");
		assertEquals("{\"data\":[{\"name\":\"XYZ\"}],\"total\":1}", toJson(p1, descriptor));
	}

	@Test
	public void testEncode_SimplePojo_Id() {

		P6 p6 = new P6();
		p6.setStringId("myid");
		p6.setIntProp(4);

		AgEntity<P6> p6age = new AgEntityBuilder<>(P6.class, new LazyAgDataMap(compilers)).build();
		ResourceEntity<P6> descriptor = new ResourceEntity<P6>(p6age);
		descriptor.getAttributes().put("intProp", new DefaultAgAttribute("intProp", Integer.class));
		descriptor.includeId();

		assertEquals("{\"data\":[{\"id\":\"myid\",\"intProp\":4}],\"total\":1}", toJson(p6, descriptor));
	}

	private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }
}
