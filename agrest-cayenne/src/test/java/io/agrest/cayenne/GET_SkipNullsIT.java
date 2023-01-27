package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_SkipNullsIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class)
            .agCustomizer(b -> b.skipNullProperties())
            .build();

    @Test
    public void testAttributes() {

        tester.e3().insertColumns("id_", "name")
                .values(6, null)
                .values(7, "yyy")
                .exec();

        tester.target("/e3")
                .queryParam("include", "id", "name")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":6}",
                        "{\"id\":7,\"name\":\"yyy\"}");
    }

    @Test
    public void testRelationships() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx").exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(6, 1)
                .values(7, null)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":6,\"e2\":{\"id\":1}}",
                        "{\"id\":7}");
    }

    @Test
    public void testRelatedAttributes() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, null).exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(6, 1)
                .values(7, 2)
                .exec();

        tester.target("/e3")
                .queryParam("include", "id", "e2")
                .queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":6,\"e2\":{\"id\":1,\"name\":\"xxx\"}}",
                        "{\"id\":7,\"e2\":{\"id\":2}}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
