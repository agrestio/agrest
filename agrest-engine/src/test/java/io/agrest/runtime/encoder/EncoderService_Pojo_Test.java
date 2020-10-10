package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.Encoders;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P6;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.property.BeanPropertyReader;
import io.agrest.runtime.semantics.RelationshipMapper;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderService_Pojo_Test {

	private static Collection<AgEntityCompiler> compilers;

	private EncoderService encoderService;

	@BeforeAll
	public static void setUpClass() {
		compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
	}

	@BeforeEach
	public void setUp() {

		IEncodablePropertyFactory epf = new EncodablePropertyFactory(new ValueEncodersProvider(Collections.emptyMap()).get());
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

		this.encoderService = new EncoderService(
				epf,
				stringConverterFactory,
				new RelationshipMapper(),
				Collections.emptyMap());
	}

	@Test
	public void testEncode_SimplePojo_noId() {
		AgEntity<P1> p1age = new AgEntityBuilder<>(P1.class, new LazyAgDataMap(compilers)).build();
		RootResourceEntity<P1> descriptor = new RootResourceEntity<>(p1age, null);
		descriptor.addAttribute(new DefaultAgAttribute("name", String.class, new ASTObjPath("name"), BeanPropertyReader.reader("name")), false);

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
		RootResourceEntity<P6> descriptor = new RootResourceEntity<>(p6age, null);
		descriptor.addAttribute(new DefaultAgAttribute("intProp", Integer.class, new ASTObjPath("intProp"), BeanPropertyReader.reader("intProp")), false);
		descriptor.includeId();

		assertEquals("{\"data\":[{\"id\":\"myid\",\"intProp\":4}],\"total\":1}", toJson(p6, descriptor));
	}

	private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }
}
