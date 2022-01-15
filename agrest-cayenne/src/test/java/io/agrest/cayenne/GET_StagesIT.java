package io.agrest.cayenne;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.cayenne.cayenne.main.E27Nopk;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.processor.select.SelectContext;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

public class GET_StagesIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E27Nopk.class)
            .build();

    @Test
    public void testNoId() {
        tester.e27NoPk().insertColumns("name")
                .values("z")
                .values("a").exec();

        tester.target("/e27")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"name\":\"a\"},{\"name\":\"z\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e27")
        public DataResponse<?> get(@Context UriInfo uriInfo) {

            // since Cayenne won't be able to fetch objects with no id, our only option is ColumnSelect and a custom encoder
            return Ag.select(E27Nopk.class, config)
                    .uri(uriInfo)
                    .terminalStage(SelectStage.APPLY_SERVER_PARAMS, this::fetchAll)
                    .get();
        }

        private void fetchAll(SelectContext<E27Nopk> context) {

            List names = ObjectSelect.columnQuery(E27Nopk.class, E27Nopk.NAME)
                    .orderBy(E27Nopk.NAME.asc())
                    .select(AgRuntime.service(ICayennePersister.class, config).sharedContext());

            context.getEntity().setData(names);

            Encoder rowEncoder = new NoIdEncoder();
            ListEncoder listEncoder = new ListEncoder(rowEncoder);
            Encoder encoder = new DataResponseEncoder("data", listEncoder, "total", GenericEncoder.encoder());
            context.setEncoder(encoder);
        }
    }

    static class NoIdEncoder implements Encoder {

        @Override
        public void encode(String propertyName, Object object, JsonGenerator out) throws IOException {

            out.writeStartObject();
            out.writeStringField("name", object == null ? null : object.toString());
            out.writeEndObject();
        }
    }
}

