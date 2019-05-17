package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.AgBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class GET_EntityOverlayIT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {

        AgEntityOverlay<E4> e4Overlay = new AgEntityOverlay<>(E4.class)
                .addAttribute("adhocString", String.class, e4 -> e4.getCVarchar() + "*")
                .addToOneRelationship("adhocToOne", EX.class, EX::forE4)
                .addToManyRelationship("adhocToMany", EY.class, EY::forE4)
                .addAttribute("derived");

        // this entity has incoming relationships
        AgEntityOverlay<E2> e2Overlay = new AgEntityOverlay<>(E2.class)
                .addAttribute("adhocString", String.class, e2 -> e2.getName() + "*");

        UnaryOperator<AgBuilder> customizer = ab -> ab.entityOverlay(e4Overlay).entityOverlay(e2Overlay);
        startTestRuntime(customizer, Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class};
    }

    @Test
    public void testAdHocMeta() {

        Response r = target("/e4/meta").request().get();

        String data = onSuccess(r).getContentAsString();

        assertTrue(data.contains("{\"name\":\"derived\",\"type\":\"string\"}"));
        assertTrue(data.contains("{\"name\":\"adhocString\",\"type\":\"string\"}"));
        assertTrue(data.contains("{\"name\":\"adhocToOne\",\"type\":\"EX\",\"relationship\":true}"));
        assertTrue(data.contains("{\"name\":\"adhocToMany\",\"type\":\"EY\",\"relationship\":true,\"collection\":true}"));
    }

    @Test
    public void testTransientAttribute() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        Response r = target("/e4")
                .queryParam("include", "derived")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2, "{\"derived\":\"x$\"},{\"derived\":\"y$\"}");
    }

    @Test
    public void testAdHocAttribute_Related() {

        e2().insertColumns("id", "name").values(1, "xxx").exec();
        e3().insertColumns("id", "name", "e2_id").values(3, "zzz", 1).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.adhocString")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":3,\"e2\":{\"adhocString\":\"xxx*\"}}");
    }

    @Test
    public void testAdHocAttribute() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        Response r = target("/e4")
                .queryParam("include", "adhocString")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2, "{\"adhocString\":\"x*\"},{\"adhocString\":\"y*\"}");
    }

    @Test
    public void testAdHocToOne() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2,
                "{\"id\":1,\"adhocToOne\":{\"p1\":\"x_\"}}",
                "{\"id\":2,\"adhocToOne\":{\"p1\":\"y_\"}}");
    }

    @Test
    public void testAdHocToMany() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        Response r = target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToMany")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2,
                "{\"id\":1,\"adhocToMany\":[{\"p1\":\"x-\"},{\"p1\":\"x%\"}]}",
                "{\"id\":2,\"adhocToMany\":[{\"p1\":\"y-\"},{\"p1\":\"y%\"}]}");
    }

    public static final class EX {

        private String p1;

        public EX(String p1) {
            this.p1 = p1;
        }

        static EX forE4(E4 e4) {
            return new EX(e4.getCVarchar() + "_");
        }

        @AgAttribute
        public String getP1() {
            return p1;
        }
    }

    public static final class EY {

        private String p1;

        public EY(String p1) {
            this.p1 = p1;
        }

        static List<EY> forE4(E4 e4) {
            return asList(
                    new EY(e4.getCVarchar() + "-"),
                    new EY(e4.getCVarchar() + "%")
            );
        }

        @AgAttribute
        public String getP1() {
            return p1;
        }
    }

    @Path("")
    public static final class Resource {
        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4/meta")
        public MetadataResponse<E4> getMetaE4(@Context UriInfo uriInfo) {
            return Ag.metadata(E4.class, config).forResource(Resource.class).uri(uriInfo).process();
        }
    }
}
