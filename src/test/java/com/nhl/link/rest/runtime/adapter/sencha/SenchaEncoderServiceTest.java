package com.nhl.link.rest.runtime.adapter.sencha;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class SenchaEncoderServiceTest extends TestWithCayenneMapping {

	private SenchaEncoderService encoderService;
	private ICayennePersister cayenneService;
	private List<EncoderFilter> filters;

	@Before
	public void before() {

		ObjectContext sharedContext = runtime.newContext();
		cayenneService = mock(ICayennePersister.class);
		when(cayenneService.sharedContext()).thenReturn(sharedContext);
		when(cayenneService.newContext()).thenReturn(runtime.newContext());

		this.filters = new ArrayList<>();
		IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactory();
		IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);
		IRelationshipMapper relationshipMapper = new SenchaRelationshipMapper();

		encoderService = new SenchaEncoderService(this.filters, attributeEncoderFactory, stringConverterFactory,
				relationshipMapper);
	}

	@Test
	public void testEncoder_FilteredToOne() throws IOException {

		filters.add(new EncoderFilter() {

			@Override
			public boolean matches(Entity<?> clientEntity) {
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

		Entity<E2> e2Descriptor = getClientEntity(E2.class);
		e2Descriptor.includeId();

		Entity<E3> e3Descriptor = getClientEntity(E3.class);
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

		DataResponse<E3> builder = DataResponse.forType(E3.class).withClientEntity(e3Descriptor).withObject(e31);

		assertEquals("[{\"id\":5,\"e2\":{\"id\":7},\"e2_id\":7}]", toJson(builder));

		E2 e22 = new E2();
		e22.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 8));
		context.registerNewObject(e22);

		E3 e32 = new E3();
		e32.setObjectId(new ObjectId("E3", E3.ID_PK_COLUMN, 6));
		context.registerNewObject(e32);
		e32.setE2(e22);

		builder.withObject(e32);

		assertEquals("[{\"id\":6}]", toJson(builder));
	}

	private String toJson(DataResponse<?> builder) throws IOException {

		Encoder encoder = encoderService.makeEncoder(builder);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try (JsonGenerator generator = new JacksonService().getJsonFactory()
				.createJsonGenerator(out, JsonEncoding.UTF8)) {
			encoder.encode(null, builder.getObjects(), generator);
		}

		return new String(out.toByteArray(), "UTF-8");
	}

}
