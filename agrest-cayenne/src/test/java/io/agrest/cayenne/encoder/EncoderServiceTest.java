package io.agrest.cayenne.encoder;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.converter.valuestring.ValueStringConvertersProvider;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.DefaultAttribute;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.encoder.EncodablePropertyFactory;
import io.agrest.runtime.encoder.EncoderService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderServiceTest extends CayenneNoDbTest {

    private static final IJacksonService jacksonService = new JacksonService();
    private EncoderService encoderService;

    @BeforeEach
    public void before() {
        ValueStringConverters converters = new ValueStringConvertersProvider(Collections.emptyMap()).get();

        IEncodablePropertyFactory epf = new EncodablePropertyFactory(
                new ValueEncodersProvider(converters, Collections.emptyMap()).get());

        encoderService = new EncoderService(
                epf,
                converters,
                new RelationshipMapper());
    }

    @Test
    public void testGetRootEncoder_ExcludedAttributes() {
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
    public void testGetRootEncoder_ExcludedRelationshipAttributes() {

        RootResourceEntity<E2> descriptor = getResourceEntity(E2.class);
        descriptor.includeId();
        CayenneProcessor.getOrCreateRootEntity(descriptor);

        ToManyResourceEntity<E3> e3Descriptor = getToManyChildEntity(E3.class, descriptor, E2.E3S.getName());
        e3Descriptor.includeId();
        CayenneProcessor.getOrCreateRelatedEntity(e3Descriptor);
        e3Descriptor.addAttribute(new DefaultAttribute("name", String.class, true, true, o -> ((E3)o).getName()), false);
        descriptor.getChildren().put(E2.E3S.getName(), e3Descriptor);

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
        descriptor.setData(Collections.singletonList(e2));
        e3Descriptor.addData(new SimpleObjectId(7), e31);
        e3Descriptor.addData(new SimpleObjectId(7), e32);

        assertEquals("{\"data\":[{\"id\":7,\"e3s\":[{\"id\":5,\"name\":\"31\"},{\"id\":6,\"name\":\"32\"}]}],\"total\":1}",
                toJson(e2, descriptor));
    }

    @Test
    public void testEncoder_BinaryAttribute() {

        ResourceEntity<E19> descriptor = getResourceEntity(E19.class);
        descriptor.includeId();
        descriptor.addAttribute(getAgEntity(E19.class).getAttribute(E19.GUID.getName()), false);

        E19 e19 = new E19();
        e19.setObjectId(ObjectId.of("E19", E19.ID_PK_COLUMN, 1));
        e19.setGuid("abcdefghjklmnopr".getBytes(StandardCharsets.UTF_8));

        assertEquals("{\"data\":[{\"id\":1,\"guid\":\"YWJjZGVmZ2hqa2xtbm9wcg==\"}],\"total\":1}", toJson(e19, descriptor));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity, mock(ProcessingContext.class));
        return toJson(encoder, DataResponse.of(200, Collections.singletonList(object)));
    }

    private String toJson(Encoder encoder, Object value) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            jacksonService.outputJson(g -> encoder.encode(null, value, g), out);
        } catch (IOException e) {
            throw new RuntimeException("Encoding error: " + e.getMessage());
        }

        return out.toString();
    }
}
