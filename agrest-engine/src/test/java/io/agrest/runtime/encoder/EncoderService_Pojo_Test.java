package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgEntity;
import io.agrest.compiler.AnnotationsAgEntityBuilder;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P6;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.ResourceEntityUtils;
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
        compilers.add(new AnnotationsAgEntityCompiler(Collections.emptyMap()));
    }

    @BeforeEach
    public void setUp() {

        IValueJsonConverterFactory converterFactory = new ValueJsonConverterFactoryProvider(Collections.emptyMap()).get();
        IEncodablePropertyFactory epf = new EncodablePropertyFactory(new ValueEncodersProvider(converterFactory, Collections.emptyMap()).get());

        this.encoderService = new EncoderService(
                epf,
                converterFactory,
                new RelationshipMapper(),
                Collections.emptyMap());
    }

    @Test
    public void testEncode_SimplePojo_noId() {
        AgEntity<P1> p1age = new AnnotationsAgEntityBuilder<>(P1.class, new LazyAgDataMap(compilers)).build();
        RootResourceEntity<P1> entity = new RootResourceEntity<>(p1age);
        ResourceEntityUtils.appendAttribute(entity, "name", String.class, P1::getName);

        P1 p1 = new P1();
        p1.setName("XYZ");
        assertEquals("{\"data\":[{\"name\":\"XYZ\"}],\"total\":1}", toJson(p1, entity));
    }

    @Test
    public void testEncode_SimplePojo_Id() {

        P6 p6 = new P6();
        p6.setStringId("myid");
        p6.setIntProp(4);

        AgEntity<P6> p6age = new AnnotationsAgEntityBuilder<>(P6.class, new LazyAgDataMap(compilers)).build();
        RootResourceEntity<P6> entity = new RootResourceEntity<>(p6age);
        ResourceEntityUtils.appendAttribute(entity, "intProp", Integer.class, P6::getIntProp);
        entity.includeId();

        assertEquals("{\"data\":[{\"id\":\"myid\",\"intProp\":4}],\"total\":1}", toJson(p6, entity));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }
}
