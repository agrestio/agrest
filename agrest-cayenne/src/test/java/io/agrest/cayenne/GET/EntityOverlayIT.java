package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.cayenne.main.E7;
import io.agrest.cayenne.cayenne.main.E8;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.AgRuntimeBuilder;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectId;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.Arrays.asList;

public class EntityOverlayIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E4.class, E7.class, E8.class)
            .agCustomizer(EntityOverlayIT::addOverlay)
            .build();

    private static AgRuntimeBuilder addOverlay(AgRuntimeBuilder builder) {

        AgEntityOverlay<E2> e2Overlay = AgEntity.overlay(E2.class)
                .attribute("adhocString", String.class, e2 -> e2.getName() + "*")
                .readablePropFilter(p -> p.property("address", false));

        AgEntityOverlay<E4> e4Overlay = AgEntity.overlay(E4.class)
                .attribute("adhocString", String.class, e4 -> e4.getCVarchar() + "*")
                .toOne("adhocToOne", EX.class, EX::forE4)
                .toMany("adhocToMany", EY.class, EY::forE4)
                .attribute("derived", String.class, E4::getDerived);

        AgEntityOverlay<E7> e7Overlay = AgEntity.overlay(E7.class)
                .relatedDataResolver("e8", e7 -> {
                    E8 e8 = new E8();
                    e8.setObjectId(ObjectId.of("e8", "id", Cayenne.intPKForObject(e7)));
                    e8.setName(e7.getName() + "_e8");
                    return e8;
                })
                // we are changing the type of the existing attribute
                .attribute("name", Integer.class, e7 -> e7.getName().length());

        return builder.entityOverlay(e4Overlay).entityOverlay(e2Overlay).entityOverlay(e7Overlay);
    }

    @Test
    public void exclude() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();

        tester.target("/e2")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"adhocString\":\"N*\",\"name\":\"N\"}");
    }

    @Test
    public void redefineAttribute_Transient() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e4")
                .queryParam("include", "derived")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2, "{\"derived\":\"x$\"},{\"derived\":\"y$\"}");
    }

    @Test
    public void redefineAttribute_AdHocRelated() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 1).exec();

        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.adhocString")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(1, "{\"id\":3,\"e2\":{\"adhocString\":\"xxx*\"}}");
    }

    @Test
    public void redefineAttribute_AdHoc() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e4")
                .queryParam("include", "adhocString")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2, "{\"adhocString\":\"x*\"},{\"adhocString\":\"y*\"}");
    }

    @Test
    public void redefineToOne_AdHoc() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToOne")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2,
                "{\"id\":1,\"adhocToOne\":{\"p1\":\"x_\"}}",
                "{\"id\":2,\"adhocToOne\":{\"p1\":\"y_\"}}");
    }

    @Test
    public void redefineToMany_AdHoc() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e4")
                .queryParam("include", "id")
                .queryParam("include", "adhocToMany")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2,
                "{\"id\":1,\"adhocToMany\":[{\"p1\":\"x-\"},{\"p1\":\"x%\"}]}",
                "{\"id\":2,\"adhocToMany\":[{\"p1\":\"y-\"},{\"p1\":\"y%\"}]}");
    }

    @Test
    public void redefineToOne_Replaced() {

        tester.e7().insertColumns("id", "name")
                .values(1, "x1")
                .values(2, "x2").exec();

        tester.target("/e7")
                .queryParam("include", "id")
                .queryParam("include", "e8.name")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2,
                "{\"id\":1,\"e8\":{\"name\":\"x1_e8\"}}",
                "{\"id\":2,\"e8\":{\"name\":\"x2_e8\"}}");
    }

    @Test
    public void redefineAttribute_Replaced() {

        tester.e7().insertColumns("id", "name")
                .values(1, "01")
                .values(2, "0123").exec();

        tester.target("/e7")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("sort", "id")
                .get().wasOk().bodyEquals(2,
                "{\"id\":1,\"name\":2}",
                "{\"id\":2,\"name\":4}");
    }

    public static final class EX {

        private final String p1;

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

        private final String p1;

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
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e7")
        public DataResponse<E7> getE7(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E7.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
