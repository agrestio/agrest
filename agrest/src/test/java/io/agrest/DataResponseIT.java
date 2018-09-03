package io.agrest;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import io.agrest.encoder.EncoderITBase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.Test;

public class DataResponseIT extends EncoderITBase {

	private String toIdsString(Collection<? extends Persistent> objects) {
		return objects.stream().map(o -> o.getObjectId().getEntityName() + ":" + Cayenne.intPKForObject(o))
				.collect(joining(";"));
	}

	@Test
	public void testGetIncludedObjects_Root_NoLimits() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DataResponse<E2> response = createLRService().select(E2.class).get();
		Collection<E2> objects = response.getIncludedObjects(E2.class, "");

		assertEquals("E2:1;E2:2;E2:3", toIdsString(objects));
	}

	@Test
	public void testGetIncludedObjects_Root_MapBy() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
		params.putSingle("mapBy", "name");

		UriInfo mockUri = mock(UriInfo.class);
		when(mockUri.getQueryParameters()).thenReturn(params);

		DataResponse<E2> response = createLRService().select(E2.class).uri(mockUri).get();
		Collection<E2> objects = response.getIncludedObjects(E2.class, "");

		assertEquals("E2:1;E2:2;E2:3", toIdsString(objects));
	}

	@Test
	public void testGetIncludedObjects_Root_StartLimit() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");
		DB.insert("e2", "id, name", "4, 'zzz'");

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
		params.putSingle("sort", "id");
		params.putSingle("start", "1");
		params.putSingle("limit", "2");

		UriInfo mockUri = mock(UriInfo.class);
		when(mockUri.getQueryParameters()).thenReturn(params);
		DataResponse<E2> response = createLRService().select(E2.class).uri(mockUri).get();

		Collection<E2> objects = response.getIncludedObjects(E2.class, "");

		assertEquals("E2:2;E2:3", toIdsString(objects));
	}

	@Test
	public void testGetIncludedObjects_Related() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DB.insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
		DB.insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
		DB.insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
		params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");

		UriInfo mockUri = mock(UriInfo.class);
		when(mockUri.getQueryParameters()).thenReturn(params);

		DataResponse<E2> response = createLRService().select(E2.class).uri(mockUri).get();
		Collection<E3> objects = response.getIncludedObjects(E3.class, "e3s");

		assertEquals("E3:8;E3:9;E3:7", toIdsString(objects));
	}

	@Test
	public void testGetIncludedObjects_MissingPath() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DB.insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
		DB.insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
		DB.insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

		DataResponse<E2> response = createLRService().select(E2.class).get();
		Collection<E3> objects = response.getIncludedObjects(E3.class, "e3s");

		assertEquals("", toIdsString(objects));
	}
}
