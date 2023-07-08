package io.agrest.cayenne.encoder;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.unit.main.MainNoDbTest;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.converter.valuestring.ValueStringConvertersProvider;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.id.AgObjectId;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.encoder.EncodablePropertyFactory;
import io.agrest.runtime.encoder.EncoderFactory;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.semantics.RelationshipMapper;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderFactoryTest extends MainNoDbTest {

    private static final IJacksonService jacksonService = JacksonService.create();
    private EncoderFactory encoderFactory;

    @BeforeEach
    public void before() {
        ValueStringConverters converters = new ValueStringConvertersProvider(Collections.emptyMap()).get();

        IEncodablePropertyFactory epf = new EncodablePropertyFactory(
                new ValueEncodersProvider(converters, Collections.emptyMap()).get());

        this.encoderFactory = new EncoderFactory(
                epf,
                converters,
                new RelationshipMapper());
    }

    @Test
    public void getRootEncoder_ExcludedAttributes() {
        // empty filter - must only include id
        ResourceEntity<E1> descriptor = getResourceEntity(E1.class);
        descriptor.includeId();

        E1 e1 = new E1();
        e1.setObjectId(ObjectId.of("E1", E1.ID_PK_COLUMN, 777));
        e1.setName("XYZ");
        e1.setAge(30);
        e1.setDescription("test");

        assertEquals("{\"data\":[{\"id\":777}],\"total\":1}", toJson(e1, descriptor));
    }

    @Test
    public void getRootEncoder_ExcludedRelationshipAttributes() {

        RootResourceEntity<E2> re = getResourceEntity(E2.class);
        re.includeId();
        CayenneProcessor.getOrCreateRootEntity(re);

        ToManyResourceEntity<E3> reE3 = getToManyChildEntity(E3.class, re, E2.E3S.getName());
        reE3.includeId();
        CayenneProcessor.getOrCreateRelatedEntity(reE3);
        reE3.ensureAttribute("name", false);
        re.ensureChild("e3s", (e, r) -> reE3);
        re.getBaseProjection().ensureRelationship("e3s");

        ObjectContext context = mockCayennePersister.newContext();
        E2 e2 = new E2();
        e2.setObjectId(ObjectId.of("E2", E2.ID__PK_COLUMN, 7));
        e2.setName("XYZ");
        e2.setAddress("bla bla street");
        context.registerNewObject(e2);

        E3 e31 = new E3();
        e31.setObjectId(ObjectId.of("E3", E3.ID__PK_COLUMN, 5));
        e31.setName("31");
        e31.setPhoneNumber("+12345678");
        context.registerNewObject(e31);
        e2.addToE3s(e31);

        E3 e32 = new E3();
        e32.setObjectId(ObjectId.of("E3", E3.ID__PK_COLUMN, 6));
        e32.setName("32");
        e31.setPhoneNumber("+87654321");
        context.registerNewObject(e32);
        e2.addToE3s(e32);

        // saves result set in ResourceEntity
        re.setData(List.of(e2));
        reE3.addData(AgObjectId.of(7), e31);
        reE3.addData(AgObjectId.of(7), e32);

        assertEquals("{\"data\":[{\"id\":7,\"e3s\":[{\"id\":5,\"name\":\"31\"},{\"id\":6,\"name\":\"32\"}]}],\"total\":1}",
                toJson(e2, re));
    }

    @Test
    public void encoder_BinaryAttribute() {

        ResourceEntity<E19> descriptor = getResourceEntity(E19.class);
        descriptor.includeId();
        descriptor.ensureAttribute("guid", false);

        E19 e19 = new E19();
        e19.setObjectId(ObjectId.of("E19", E19.ID_PK_COLUMN, 1));
        e19.setGuid("abcdefghjklmnopr".getBytes(StandardCharsets.UTF_8));

        assertEquals("{\"data\":[{\"id\":1,\"guid\":\"YWJjZGVmZ2hqa2xtbm9wcg==\"}],\"total\":1}", toJson(e19, descriptor));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderFactory.encoder(resourceEntity, mock(ProcessingContext.class));
        return toJson(encoder, DataResponse.of(200, List.of(object)).build());
    }

    private String toJson(Encoder encoder, Object value) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            jacksonService.outputJson(g -> encoder.encode(null, value, false, g), out);
        } catch (IOException e) {
            throw new RuntimeException("Encoding error: " + e.getMessage());
        }

        return out.toString();
    }
}
