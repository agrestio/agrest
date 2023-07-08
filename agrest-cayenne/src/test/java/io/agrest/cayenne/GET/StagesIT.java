package io.agrest.cayenne.GET;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E27Nopk;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.runtime.processor.select.SelectContext;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class StagesIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E3.class, E27Nopk.class)
            .build();

    @Test
    public void terminalStageOnSTART() {
        tester.target("/e3-terminate-on-START")
                .get()
                .wasOk()
                .bodyEquals("{\"data\":[],\"total\":0}");
    }

    @Test
    public void terminalStageOnAPPLY_SERVER_PARAMS() {
        tester.e27NoPk().insertColumns("name")
                .values("z")
                .values("a").exec();

        tester.target("/e27-terminate-on-APPLY_SERVER_PARAMS")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"name\":\"a\"},{\"name\":\"z\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3-terminate-on-START")
        public DataResponse<E3> e27_TerminateOnSTART(@Context UriInfo uriInfo) {

            // terminate early with no action - must result in an empty DataResponse
            return AgJaxrs.select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .terminalStage(SelectStage.START, c -> {
                    })
                    .get();
        }

        @GET
        @Path("e27-terminate-on-APPLY_SERVER_PARAMS")
        public DataResponse<E27Nopk> e27(@Context UriInfo uriInfo) {

            // since Cayenne won't be able to fetch objects with no id, our only option is ColumnSelect and a custom encoder
            return AgJaxrs.select(E27Nopk.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .terminalStage(SelectStage.APPLY_SERVER_PARAMS, this::fetchAll)
                    .get();
        }

        private void fetchAll(SelectContext<E27Nopk> context) {

            List names = ObjectSelect.columnQuery(E27Nopk.class, E27Nopk.NAME)
                    .orderBy(E27Nopk.NAME.asc())
                    .select(AgJaxrs.runtime(config).service(ICayennePersister.class).sharedContext());

            context.getEntity().setData(names);

            Encoder encoder = DataResponseEncoder.withElementEncoder(new NoIdEncoder());
            context.setEncoder(encoder);
        }
    }

    static class NoIdEncoder implements Encoder {

        @Override
        public void encode(String propertyName, Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
            out.writeStartObject();
            out.writeStringField("name", object == null ? null : object.toString());
            out.writeEndObject();
        }
    }
}

