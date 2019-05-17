package io.agrest.encoder;

import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Encoder_VisitIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        JerseyAndDerbyCase.startTestRuntime();
    }

    static String responseContents(DataResponse<?> response) {
        return responseContents(response, new IdCountingVisitor());
    }

    static String responseContents(DataResponse<?> response, IdCountingVisitor visitor) {
        response.getEncoder().visitEntities(response.getObjects(), visitor);
        return visitor.visited + ";" + visitor.ids.stream().collect(joining(";"));
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void testNoLimits() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        DataResponse<E2> response = ag().select(E2.class).get();
        assertEquals("3;E2:1;E2:2;E2:3", responseContents(response));
    }

    @Test
    public void testStartLimit() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz")
                .values(4, "zzz").exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "id");
        params.putSingle("start", "1");
        params.putSingle("limit", "2");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);
        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();

        assertEquals("2;E2:2;E2:3", responseContents(response));
    }

    @Test
    public void testVisitorLimit() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        IdCountingVisitor visitor = new IdCountingVisitor();
        visitor.remainingNodes = 2;

        DataResponse<E2> response = ag().select(E2.class).get();
        assertEquals("2;E2:1;E2:2", responseContents(response, visitor));
    }

    @Test
    public void testRelated() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();

        assertEquals("6;E2:1;E3:8;E3:9;E2:2;E3:7;E2:3", responseContents(response));
    }

    static class IdCountingVisitor implements EncoderVisitor {

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
