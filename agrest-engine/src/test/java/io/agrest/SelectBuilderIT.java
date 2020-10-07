package io.agrest;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;
import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SelectBuilderIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester().build();

    @Test
    public void testGetIncludedObjects_Root_NoLimits() {

        tester.bucket(Tr.class).put(1, new Tr(1, "a"));
        tester.bucket(Tr.class).put(2, new Tr(2, "b"));
        tester.bucket(Tr.class).put(3, new Tr(3, "c"));

        DataResponse<Tr> response = tester.ag().select(Tr.class).get();
        String names = response.getIncludedObjects(Tr.class, "").stream().map(Tr::getName).collect(joining(","));
        assertEquals("a,b,c", names);
    }

    @Test
    public void testGetIncludedObjects_Root_MapBy() {

        tester.bucket(Tr.class).put(1, new Tr(1, "a"));
        tester.bucket(Tr.class).put(2, new Tr(2, "b"));
        tester.bucket(Tr.class).put(3, new Tr(3, "c"));

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("mapBy", "name");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<Tr> response = tester.ag().select(Tr.class).uri(mockUri).get();
        String names = response.getIncludedObjects(Tr.class, "").stream().map(Tr::getName).collect(joining(","));
        assertEquals("a,b,c", names);
    }

    @Test
    public void testGetIncludedObjects_Root_StartLimit() {

        tester.bucket(Tr.class).put(1, new Tr(1, "a"));
        tester.bucket(Tr.class).put(2, new Tr(2, "b"));
        tester.bucket(Tr.class).put(3, new Tr(3, "c"));
        tester.bucket(Tr.class).put(4, new Tr(4, "d"));

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "id");
        params.putSingle("start", "1");
        params.putSingle("limit", "2");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);
        DataResponse<Tr> response = tester.ag().select(Tr.class).uri(mockUri).get();

        String names = response.getIncludedObjects(Tr.class, "").stream().map(Tr::getName).collect(joining(","));
        assertEquals("b,c", names);
    }

    @Test
    public void testGetIncludedObjects_Related() {

        Ts ts1 = new Ts(11, "p");
        Ts ts2 = new Ts(12, "q");
        Ts ts3 = new Ts(13, "r");

        tester.bucket(Ts.class).put(11, ts1);
        tester.bucket(Ts.class).put(12, ts1);
        tester.bucket(Ts.class).put(13, ts1);

        tester.bucket(Tr.class).put(1, new Tr(1, "a", ts1, ts2));
        tester.bucket(Tr.class).put(2, new Tr(2, "b", ts3));
        tester.bucket(Tr.class).put(3, new Tr(3, "c"));

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"rtss\",\"sort\":\"id\"}");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<Tr> response = tester.ag().select(Tr.class).uri(mockUri).get();
        String names = response.getIncludedObjects(Ts.class, "rtss").stream().map(Ts::getName).collect(joining(","));

        assertEquals("p,q,r", names);
    }

    @Test
    public void testGetIncludedObjects_MissingPath() {

        Ts ts1 = new Ts(11, "p");
        Ts ts2 = new Ts(12, "q");
        Ts ts3 = new Ts(13, "r");

        tester.bucket(Ts.class).put(11, ts1);
        tester.bucket(Ts.class).put(12, ts1);
        tester.bucket(Ts.class).put(13, ts1);

        tester.bucket(Tr.class).put(1, new Tr(1, "a", ts1, ts2));
        tester.bucket(Tr.class).put(2, new Tr(2, "b", ts3));
        tester.bucket(Tr.class).put(3, new Tr(3, "c"));

        DataResponse<Tr> response = tester.ag().select(Tr.class).get();
        Collection<Ts> objects = response.getIncludedObjects(Ts.class, "rtss");

        assertTrue(objects.isEmpty());
    }

    public static class Tr {
        private final int id;
        private final String name;
        private final List<Ts> rtss;

        public Tr(int id, String name, Ts... tss) {
            this.id = id;
            this.name = name;
            this.rtss = asList(tss);
        }

        @AgAttribute
        public int getId() {
            return id;
        }

        @AgAttribute
        public String getName() {
            return name;
        }

        @AgRelationship
        public List<Ts> getRtss() {
            return rtss;
        }
    }

    public static class Ts {
        private final int id;
        private final String name;

        public Ts(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @AgAttribute
        public int getId() {
            return id;
        }

        @AgAttribute
        public String getName() {
            return name;
        }
    }
}
