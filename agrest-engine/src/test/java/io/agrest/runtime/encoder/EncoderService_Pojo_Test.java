package io.agrest.runtime.encoder;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.converter.valuestring.ValueStringConvertersProvider;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P6;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.semantics.RelationshipMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderService_Pojo_Test {

    static final AgSchema schema = new LazySchema(List.of(new AnnotationsAgEntityCompiler(Map.of())));

    private EncoderService encoderService;

    @BeforeEach
    public void setUp() {

        ValueStringConverters converters = new ValueStringConvertersProvider(Collections.emptyMap()).get();
        IEncodablePropertyFactory epf = new EncodablePropertyFactory(new ValueEncodersProvider(converters, Collections.emptyMap()).get());

        this.encoderService = new EncoderService(
                epf,
                converters,
                new RelationshipMapper());
    }

    @Test
    public void testEncode_SimplePojo_noId() {
        AgEntity<P1> p1e = schema.getEntity(P1.class);
        RootResourceEntity<P1> re = new RootResourceEntity<>(p1e);
        re.ensureAttribute("name", false);

        P1 p1 = new P1();
        p1.setName("XYZ");
        assertEquals("{\"data\":[{\"name\":\"XYZ\"}],\"total\":1}", toJson(p1, re));
    }

    @Test
    public void testEncode_SimplePojo_Id() {

        AgEntity<P6> p6e = schema.getEntity(P6.class);

        P6 p6 = new P6();
        p6.setStringId("myid");
        p6.setIntProp(4);

        RootResourceEntity<P6> re = new RootResourceEntity<>(p6e);
        re.ensureAttribute("intProp", false);
        re.includeId();

        assertEquals("{\"data\":[{\"id\":\"myid\",\"intProp\":4}],\"total\":1}", toJson(p6, re));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity, mock(ProcessingContext.class));
        return Encoders.toJson(DataResponse.of(List.of(object)).encoder(encoder).build());
    }
}
