package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GET_Props_AdHocProperties_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E4Resource.class);
    }

    @Override
    protected LinkRestBuilder doConfigure() {
        // try all possible adhoc properties...
        LrEntityOverlay<E4> overlay = new LrEntityOverlay<>(E4.class)
                .addAttribute("adhocString", String.class, e4 -> e4.getCVarchar() + "*")
                .addToOneRelationship("adhocToOne", EX.class, EX::forE4)
                .addToManyRelationship("adhocToMany", EY.class, EY::forE4)
                .addAttribute("derived");

        return super.doConfigure().entityOverlay(overlay);
    }

    @Test
    public void testAdHocMeta() {

        Response r = target("/e4/meta").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String data = r.readEntity(String.class);
        assertTrue(data.contains("{\"name\":\"derived\",\"type\":\"unknown\"}"));
        assertTrue(data.contains("{\"name\":\"adhocString\",\"type\":\"string\"}"));
        assertTrue(data.contains("{\"name\":\"adhocToOne\",\"type\":\"EX\",\"relationship\":true}"));
        assertTrue(data.contains("{\"name\":\"adhocToMany\",\"type\":\"EY\",\"relationship\":true,\"collection\":true}"));
    }

    @Test
    public void testTransientAttribute() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "derived")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"derived\":\"x$\"},{\"derived\":\"y$\"}],\"total\":2}",
                r.readEntity(String.class));
    }

    @Test
    public void testAdHocAttribute() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "adhocString")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"adhocString\":\"x*\"},{\"adhocString\":\"y*\"}],\"total\":2}",
                r.readEntity(String.class));
    }

    @Test
    public void testAdHocToOne() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"adhocToOne\":{\"p1\":\"x_\"}}," +
                        "{\"id\":2,\"adhocToOne\":{\"p1\":\"y_\"}}]," +
                        "\"total\":2}",
                r.readEntity(String.class));
    }

    @Test
    public void testAdHocToMany() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToMany")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"adhocToMany\":[{\"p1\":\"x-\"},{\"p1\":\"x%\"}]}," +
                        "{\"id\":2,\"adhocToMany\":[{\"p1\":\"y-\"},{\"p1\":\"y%\"}]}]," +
                        "\"total\":2}",
                r.readEntity(String.class));
    }

    public static final class EX {

        static EX forE4(E4 e4) {
            return new EX(e4.getCVarchar() + "_");
        }

        private String p1;

        public EX(String p1) {
            this.p1 = p1;
        }

        @LrAttribute
        public String getP1() {
            return p1;
        }
    }

    public static final class EY {

        static List<EY> forE4(E4 e4) {
            return asList(
                    new EY(e4.getCVarchar() + "-"),
                    new EY(e4.getCVarchar() + "%")
            );
        }

        private String p1;

        public EY(String p1) {
            this.p1 = p1;
        }

        @LrAttribute
        public String getP1() {
            return p1;
        }
    }
}
