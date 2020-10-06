package io.agrest.sencha.runtime.encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.runtime.encoder.AttributeEncoderFactory;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.encoder.ValueEncodersProvider;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.sencha.runtime.semantics.SenchaRelationshipMapper;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SenchaEncoderServiceTest extends CayenneNoDbTest {

    private SenchaEncoderService encoderService;
    private ICayennePersister cayenneService;

    @Before
    public void before() {

        ObjectContext sharedContext = CayenneNoDbTest.runtime.newContext();
        cayenneService = mock(ICayennePersister.class);
        when(cayenneService.sharedContext()).thenReturn(sharedContext);
        when(cayenneService.newContext()).thenReturn(CayenneNoDbTest.runtime.newContext());

        IAttributeEncoderFactory aef = new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get());
        IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
        IRelationshipMapper relationshipMapper = new SenchaRelationshipMapper();

        encoderService = new SenchaEncoderService(
                aef,
                stringConverterFactory,
                relationshipMapper,
                Collections.emptyMap());
    }

    @Test
    public void testEncoder_FilteredToOne() throws IOException {

        EntityEncoderFilter filter = EntityEncoderFilter.forAll()
                .objectCondition((p, o, d) -> o instanceof E2 && Cayenne.intPKForObject((E2) o) != 7
                        ? false : d.willEncode(p, o)
                )
                .encoder((p, o, out, d) -> o instanceof E2 && Cayenne.intPKForObject((E2) o) != 7
                        ? false : d.encode(p, o, out))
                .build();


        RootResourceEntity<E3> e3Descriptor = getResourceEntity(E3.class);
        e3Descriptor.getEntityEncoderFilters().add(filter);
        e3Descriptor.includeId();

        NestedResourceEntity<E2> e2Descriptor = getChildResourceEntity(E2.class, e3Descriptor, E3.E2.getName());
        e2Descriptor.getEntityEncoderFilters().add(filter);
        e2Descriptor.includeId();
        e3Descriptor.getChildren().put(E3.E2.getName(), e2Descriptor);

        ObjectContext context = cayenneService.newContext();

        E2 e21 = new E2();
        e21.setObjectId(new ObjectId("E2", E2.ID__PK_COLUMN, 7));
        context.registerNewObject(e21);

        E3 e31 = new E3();
        e31.setObjectId(new ObjectId("E3", E3.ID__PK_COLUMN, 5));
        context.registerNewObject(e31);
        e31.setE2(e21);

        // saves result set in ResourceEntity
        e3Descriptor.setResult(Collections.singletonList(e31));
        e2Descriptor.setToOneResult(new SimpleObjectId(5), e21);

        assertEquals("{\"success\":true,\"data\":[{\"id\":5,\"e2\":{\"id\":7},\"e2_id\":7}],\"total\":1}",
                toJson(e31, e3Descriptor));

        E2 e22 = new E2();
        e22.setObjectId(new ObjectId("E2", E2.ID__PK_COLUMN, 8));
        context.registerNewObject(e22);

        E3 e32 = new E3();
        e32.setObjectId(new ObjectId("E3", E3.ID__PK_COLUMN, 6));
        context.registerNewObject(e32);
        e32.setE2(e22);

        // saves result set in ResourceEntity
        e3Descriptor.setResult(Collections.singletonList(e32));
        e2Descriptor.setToOneResult(new SimpleObjectId(6), e22);

        assertEquals("{\"success\":true,\"data\":[{\"id\":6}],\"total\":1}", toJson(e32, e3Descriptor));
    }

    private String toJson(Object object, ResourceEntity<?> entity) throws IOException {

        Encoder encoder = encoderService.dataEncoder(entity);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (JsonGenerator generator = new JacksonService().getJsonFactory().createGenerator(out, JsonEncoding.UTF8)) {
            encoder.encode(null, Collections.singletonList(object), generator);
        }

        return new String(out.toByteArray(), "UTF-8");
    }

}
