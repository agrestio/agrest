package io.agrest.sencha.runtime.encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.encoder.AttributeEncoderFactoryProvider;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.sencha.runtime.semantics.SenchaRelationshipMapper;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SenchaEncoderServiceTest extends TestWithCayenneMapping {

    private SenchaEncoderService encoderService;
    private ICayennePersister cayenneService;
    private List<EncoderFilter> filters;

    @Before
    public void before() {

        ObjectContext sharedContext = TestWithCayenneMapping.runtime.newContext();
        cayenneService = mock(ICayennePersister.class);
        when(cayenneService.sharedContext()).thenReturn(sharedContext);
        when(cayenneService.newContext()).thenReturn(TestWithCayenneMapping.runtime.newContext());

        this.filters = new ArrayList<>();
        IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactoryProvider(Collections.emptyMap()).get();
        IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
        IRelationshipMapper relationshipMapper = new SenchaRelationshipMapper();

        encoderService = new SenchaEncoderService(this.filters, attributeEncoderFactory, stringConverterFactory,
                relationshipMapper, Collections.<String, PropertyMetadataEncoder>emptyMap());
    }

    @Test
    public void testEncoder_FilteredToOne() throws IOException {

        filters.add(new EncoderFilter() {

            @Override
            public boolean matches(ResourceEntity<?> entity) {
                return true;
            }

            @Override
            public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
                    throws IOException {

                if (object instanceof E2) {
                    E2 e2 = (E2) object;
                    if (Cayenne.intPKForObject(e2) == 7) {
                        return delegate.encode(propertyName, object, out);
                    } else {
                        return false;
                    }
                } else {
                    delegate.encode(propertyName, object, out);
                    return true;
                }
            }

            @Override
            public boolean willEncode(String propertyName, Object object, Encoder delegate) {
                if (object instanceof E2) {
                    E2 e2 = (E2) object;
                    if (Cayenne.intPKForObject(e2) == 7) {
                        return delegate.willEncode(propertyName, object);
                    } else {
                        return false;
                    }
                } else {
                    return delegate.willEncode(propertyName, object);
                }
            }
        });

        ResourceEntity<E2> e2Descriptor = getResourceEntity(E2.class);
        e2Descriptor.includeId();

        ResourceEntity<E3> e3Descriptor = getResourceEntity(E3.class);
        e3Descriptor.includeId();
        e3Descriptor.getChildren().put(E3.E2.getName(), e2Descriptor);

        ObjectContext context = cayenneService.newContext();

        E2 e21 = new E2();
        e21.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 7));
        context.registerNewObject(e21);

        E3 e31 = new E3();
        e31.setObjectId(new ObjectId("E3", E3.ID_PK_COLUMN, 5));
        context.registerNewObject(e31);
        e31.setE2(e21);

        // saves result set in ResourceEntity
        e3Descriptor.setResult(Collections.singletonList(e31));
        e2Descriptor.addToOneResult(new SimpleObjectId(5), e21);

        assertEquals("{\"success\":true,\"data\":[{\"id\":5,\"e2\":{\"id\":7},\"e2_id\":7}],\"total\":1}",
                toJson(e31, e3Descriptor));

        E2 e22 = new E2();
        e22.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 8));
        context.registerNewObject(e22);

        E3 e32 = new E3();
        e32.setObjectId(new ObjectId("E3", E3.ID_PK_COLUMN, 6));
        context.registerNewObject(e32);
        e32.setE2(e22);

        // saves result set in ResourceEntity
        e3Descriptor.setResult(Collections.singletonList(e32));
        e2Descriptor.addToOneResult(new SimpleObjectId(6), e22);

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
