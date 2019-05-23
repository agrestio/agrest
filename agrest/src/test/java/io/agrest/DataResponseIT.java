package io.agrest;

import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataResponseIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        JerseyAndDerbyCase.startTestRuntime();
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    private String toIdsString(Collection<? extends Persistent> objects) {
        return objects.stream().map(o -> o.getObjectId().getEntityName() + ":" + Cayenne.intPKForObject(o))
                .collect(joining(";"));
    }

    @Test
    public void testGetIncludedObjects_Root_NoLimits() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        DataResponse<E2> response = ag().select(E2.class).get();
        Collection<E2> objects = response.getIncludedObjects(E2.class, "");

        assertEquals("E2:1;E2:2;E2:3", toIdsString(objects));
    }

    @Test
    public void testGetIncludedObjects_Root_MapBy() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("mapBy", "name");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();
        Collection<E2> objects = response.getIncludedObjects(E2.class, "");

        assertEquals("E2:1;E2:2;E2:3", toIdsString(objects));
    }

    @Test
    public void testGetIncludedObjects_Root_StartLimit() {

        e2().insertColumns("id_", "name")
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

        Collection<E2> objects = response.getIncludedObjects(E2.class, "");

        assertEquals("E2:2;E2:3", toIdsString(objects));
    }

    @Test
    public void testGetIncludedObjects_Related() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();
        Collection<E3> objects = response.getIncludedObjects(E3.class, "e3s");

        assertEquals("E3:8;E3:9;E3:7", toIdsString(objects));
    }

    @Test
    public void testGetIncludedObjects_MissingPath() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        DataResponse<E2> response = ag().select(E2.class).get();
        Collection<E3> objects = response.getIncludedObjects(E3.class, "e3s");

        assertEquals("", toIdsString(objects));
    }
}
