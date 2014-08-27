package com.nhl.link.rest.runtime.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E1;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class EncoderServiceTest extends TestWithCayenneMapping {

	private EncoderService encoderService;
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

		encoderService = new EncoderService(this.filters, attributeEncoderFactory, stringConverterFactory,
				new RelationshipMapper());
	}

	@Test
	public void testGetRootEncoder_ExcludedAttributes() throws IOException {
		// empty filter - must only include id
		Entity<E1> descriptor = getClientEntity(E1.class);
		descriptor.includeId();

		E1 e1 = new E1();
		e1.setObjectId(new ObjectId("E1", E1.ID_PK_COLUMN, 777));
		e1.setName("XYZ");
		e1.setAge(30);
		e1.setDescription("test");

		DataResponse<E1> builder = DataResponse.forType(E1.class).withClientEntity(descriptor)
				.withObjects(Collections.singletonList(e1));

		assertEquals("[{\"id\":777}]", toJson(builder));
	}

	@Test
	public void testGetRootEncoder_ExcludedRelationshipAttributes() throws IOException {
		Entity<E3> e3Descriptor = getClientEntity(E3.class);
		e3Descriptor.includeId();
		e3Descriptor.getAttributes().add(E3.NAME.getName());

		Entity<E2> descriptor = getClientEntity(E2.class);
		descriptor.includeId();
		descriptor.getChildren().put(E2.E3S.getName(), e3Descriptor);

		ObjectContext context = cayenneService.newContext();
		E2 e2 = new E2();
		e2.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 7));
		e2.setName("XYZ");
		e2.setAddress("bla bla street");
		context.registerNewObject(e2);

		E3 e31 = new E3();
		e31.setObjectId(new ObjectId("E3", E3.ID_PK_COLUMN, 5));
		e31.setName("31");
		e31.setPhoneNumber("+12345678");
		context.registerNewObject(e31);
		e2.addToE3s(e31);

		E3 e32 = new E3();
		e32.setObjectId(new ObjectId("E3", E3.ID_PK_COLUMN, 6));
		e32.setName("32");
		e31.setPhoneNumber("+87654321");
		context.registerNewObject(e32);
		e2.addToE3s(e32);

		DataResponse<E2> builder = DataResponse.forType(E2.class).withClientEntity(descriptor)
				.withObjects(Collections.singletonList(e2));

		assertEquals("[{\"id\":7,\"e3s\":[{\"id\":5,\"name\":\"31\"},{\"id\":6,\"name\":\"32\"}]}]", toJson(builder));
	}

	@Test
	public void testEncoder_FilteredRoots() throws IOException {

		filters.add(new EncoderFilter() {

			@Override
			public boolean matches(Entity<?> clientEntity) {
				return true;
			}

			@Override
			public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
					throws IOException {

				E2 e2 = (E2) object;
				if (Cayenne.intPKForObject(e2) == 7) {
					delegate.encode(propertyName, object, out);
					return true;
				}

				return false;
			}

			@Override
			public boolean willEncode(String propertyName, Object object, Encoder delegate) {
				E2 e2 = (E2) object;
				if (Cayenne.intPKForObject(e2) == 7) {
					delegate.willEncode(propertyName, object);
					return true;
				}

				return false;
			}
		});

		Entity<E2> descriptor = getClientEntity(E2.class);
		descriptor.includeId();

		ObjectContext context = cayenneService.newContext();
		E2 e21 = new E2();
		e21.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 7));
		e21.setName("XYZ");
		e21.setAddress("bla bla street");
		context.registerNewObject(e21);

		DataResponse<E2> builder = DataResponse.forType(E2.class).withClientEntity(descriptor).withObject(e21);

		assertEquals("[{\"id\":7}]", toJson(builder));

		E2 e22 = new E2();
		e22.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 8));
		e22.setName("XYZ");
		e22.setAddress("bla bla street");
		context.registerNewObject(e22);

		builder.withObject(e22);

		assertEquals("[]", toJson(builder));
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

		assertEquals("[{\"id\":5,\"e2\":{\"id\":7}}]", toJson(builder));

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

	@Test
	public void testEncoder_FilterNoMatch() throws IOException {

		filters.add(new EncoderFilter() {

			@Override
			public boolean matches(Entity<?> clientEntity) {
				return false;
			}

			@Override
			public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
					throws IOException {

				fail("Non matching filter was not supposed to be invoked");
				return false;
			}

			@Override
			public boolean willEncode(String propertyName, Object object, Encoder delegate) {
				fail("Non matching filter was not supposed to be invoked");
				return false;
			}
		});

		Entity<E2> descriptor = getClientEntity(E2.class);
		descriptor.includeId();

		DataResponse<E2> builder = DataResponse.forType(E2.class).withClientEntity(descriptor);

		ObjectContext context = cayenneService.newContext();
		E2 e21 = new E2();
		e21.setObjectId(new ObjectId("E2", E2.ID_PK_COLUMN, 7));
		e21.setName("XYZ");
		e21.setAddress("bla bla street");
		context.registerNewObject(e21);

		builder.getEncoder().encode(null, Collections.singletonList(e21), mock(JsonGenerator.class));
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
