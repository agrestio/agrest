package io.agrest.encoder;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;

public class Encoder_VisitIT extends EncoderITBase {

	private String responseContents(DataResponse<?> response) {
		return responseContents(response, new IdCountingVisitor());
	}

	private String responseContents(DataResponse<?> response, IdCountingVisitor visitor) {
		response.getEncoder().visitEntities(response.getObjects(), visitor);
		return visitor.visited + ";" + visitor.ids.stream().collect(joining(";"));
	}

	@Test
	public void testVisit_NoLimits() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DataResponse<E2> response = createAgService().select(E2.class).get();
		assertEquals("3;E2:1;E2:2;E2:3", responseContents(response));
	}

	@Test
	public void testVisit_StartLimit() {

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
		DataResponse<E2> response = createAgService().select(E2.class).uri(mockUri).get();

		assertEquals("2;E2:2;E2:3", responseContents(response));
	}

	@Test
	public void testVisit_EncoderFilter() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");
		DB.insert("e2", "id, name", "4, 'zzz'");

		DataResponse<E2> response = createAgService(new TestFilter(1, 3)).select(E2.class).get();
		assertEquals("2;E2:1;E2:3", responseContents(response));
	}

	@Test
	public void testVisit_VisitorLimit() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		IdCountingVisitor visitor = new IdCountingVisitor();
		visitor.remainingNodes = 2;

		DataResponse<E2> response = createAgService().select(E2.class).get();
		assertEquals("2;E2:1;E2:2", responseContents(response, visitor));
	}

	@Test
	public void testVisit_Related() {

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

		DataResponse<E2> response = createAgService().select(E2.class).uri(mockUri).get();

		assertEquals("6;E2:1;E3:8;E3:9;E2:2;E3:7;E2:3", responseContents(response));
	}

	class TestFilter implements EncoderFilter {

		private Collection<Integer> ids;

		TestFilter(Integer... idsToEncode) {
			this.ids = asList(idsToEncode);
		}

		@Override
		public boolean matches(ResourceEntity<?> entity) {
			return true;
		}

		@Override
		public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
				throws IOException {

			if (willEncode(propertyName, object, delegate)) {
				return delegate.encode(propertyName, object, out);
			}

			return false;
		}

		@Override
		public boolean willEncode(String propertyName, Object object, Encoder delegate) {
			return ids.contains(Cayenne.intPKForObject((Persistent) object));
		}
	}

	class IdCountingVisitor implements EncoderVisitor {

		List<String> ids = new ArrayList<>();
		int remainingNodes = Integer.MAX_VALUE;
		int visited;

		@Override
		public int visit(Object object) {

			if (remainingNodes == 0) {
				return Encoder.VISIT_SKIP_ALL;
			}

			visited++;

			remainingNodes--;
			Persistent p = (Persistent) object;
			ids.add(p.getObjectId().getEntityName() + ":" + Cayenne.intPKForObject(p));

			return Encoder.VISIT_CONTINUE;
		}
	}
}
