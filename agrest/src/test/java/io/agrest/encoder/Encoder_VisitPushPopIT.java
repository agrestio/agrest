package io.agrest.encoder;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import io.agrest.DataResponse;
import io.agrest.it.fixture.cayenne.E2;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.Test;

public class Encoder_VisitPushPopIT extends EncoderITBase {

	private String responseContents(DataResponse<?> response, PushPopVisitor visitor) {
		response.getEncoder().visitEntities(response.getObjects(), visitor);
		return visitor.ids.stream().collect(joining(";"));
	}

	@Test
	public void testVisit_Tree() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DB.insert("e5", "id, name", "1, 'xxx'");
		DB.insert("e5", "id, name", "2, 'yyy'");

		DB.insert("e3", "id, e2_id, e5_id, name", "7, 2, 1, 'zzz'");
		DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 1, 'yyy'");
		DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 2, 'zzz'");

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
		params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");
		params.putSingle("include", "e3s.e5");

		UriInfo mockUri = mock(UriInfo.class);
		when(mockUri.getQueryParameters()).thenReturn(params);

		DataResponse<E2> response = createLRService().select(E2.class).uri(mockUri).get();

		PushPopVisitor visitor = new PushPopVisitor();

		assertEquals("E3:8;E3:9;E3:7", responseContents(response, visitor));
	}
	
	@Test
	public void testVisit_Tree_MapBy() {

		DB.insert("e2", "id, name", "1, 'xxx'");
		DB.insert("e2", "id, name", "2, 'yyy'");
		DB.insert("e2", "id, name", "3, 'zzz'");

		DB.insert("e5", "id, name", "1, 'xxx'");
		DB.insert("e5", "id, name", "2, 'yyy'");

		DB.insert("e3", "id, e2_id, e5_id, name", "7, 2, 1, 'zzz'");
		DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 1, 'yyy'");
		DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 2, 'zzz'");

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
		params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");
		params.putSingle("include", "e3s.e5");
		params.putSingle("mapBy", "name");

		UriInfo mockUri = mock(UriInfo.class);
		when(mockUri.getQueryParameters()).thenReturn(params);

		DataResponse<E2> response = createLRService().select(E2.class).uri(mockUri).get();

		PushPopVisitor visitor = new PushPopVisitor();

		assertEquals("E3:8;E3:9;E3:7", responseContents(response, visitor));
	}

	class PushPopVisitor implements EncoderVisitor {

		String processPath = "e3s";
		Deque<String> stack = new ArrayDeque<>();
		List<String> ids = new ArrayList<>();
		boolean terminal;

		@Override
		public int visit(Object object) {

			if (terminal) {
				Persistent p = (Persistent) object;
				ids.add(p.getObjectId().getEntityName() + ":" + Cayenne.intPKForObject(p));
				return Encoder.VISIT_SKIP_CHILDREN;
			}

			return Encoder.VISIT_CONTINUE;
		}

		@Override
		public void push(String relationship) {
			stack.push(relationship);
			terminal = String.join(".", stack).equals(processPath);
		}

		@Override
		public void pop() {
			stack.pop();
			terminal = String.join(".", stack).equals(processPath);
		}
	}
}
