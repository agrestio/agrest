package com.nhl.link.rest.it;

import com.nhl.link.rest.encoder.DateTimeFormatters;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.parser.converter.UtcDateConverter;
import com.nhl.link.rest.swagger.api.v1.service.E1Resource;
import com.nhl.link.rest.swagger.api.v1.service.E2Resource;
import com.nhl.link.rest.swagger.api.v1.service.E3Resource;
import com.nhl.link.rest.swagger.api.v1.service.E4Resource;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;

public class GET_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E1Resource.class);
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(E4Resource.class);
    }

    @Test
    public void testResponse() {

        insert("e4", "id, c_varchar, c_int", "1, 'xxx', 5");

        Response response = target("/v1/e4").request().get();
        onSuccess(response).bodyEquals(1,
                "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                        + "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void testDateTime() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03T11:01:02Z")));

        SQLTemplate insert = new SQLTemplate(E4.class,
                "INSERT INTO utest.e4 (c_timestamp) values (#bind($ts 'TIMESTAMP'))");
        insert.setParams(Collections.singletonMap("ts", date));
        newContext().performGenericQuery(insert);

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        Response response = target("/v1/e4").queryParam("include", E4.C_TIMESTAMP.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cTimestamp\":\"" + dateString + "\"}");
    }

    @Test
    public void testDate() {

        Date date = Date.from(Instant.from(UtcDateConverter.dateParser().fromString("2012-02-03")));

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (c_date) values (#bind($date 'DATE'))");
        insert.setParams(Collections.singletonMap("date", date));
        newContext().performGenericQuery(insert);

        String dateString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));

        Response response = target("/v1/e4").queryParam("include", E4.C_DATE.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cDate\":\"" + dateString + "\"}");
    }

    @Test
    public void testTime() {

        LocalTime lt = LocalTime.of(14, 0, 1);

        // "14:00:01"
        Time time = Time.valueOf(lt);

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (c_time) values (#bind($time 'TIME'))");
        insert.setParams(Collections.singletonMap("time", time));
        newContext().performGenericQuery(insert);

        String timeString = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(time.getTime()));

        Response response = target("/v1/e4").queryParam("include", E4.C_TIME.getName()).request().get();
        onSuccess(response).bodyEquals(1, "{\"cTime\":\"" + timeString + "\"}");
    }

    // TODO: add tests for java.sql attributes

    @Test
    public void test_Sort_ById() {

        insert("e4", "id", "2");
        insert("e4", "id", "1");
        insert("e4", "id", "3");

        Response response = target("/v1/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void test_Sort_Invalid() {

        Response response = target("/v1/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"xyz\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onResponse(response)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Invalid path 'xyz' for 'E4'\"}");
    }

    @Test
    public void test_SelectById() {

        insert("e4", "id", "2");

        Response response = target("/v1/e4/2").request().get();

        onSuccess(response).bodyEquals(1, "{\"id\":2,\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":null}");
    }

    @Test
    public void test_SelectById_Params() {

        insert("e4", "id", "2");

        Response response1 = target("/v1/e4/2").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                + "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}");

        Response response2 = target("/v1/e4/2").queryParam("include", "id").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void test_SelectById_NotFound() {

        Response response = target("/v1/e4/2").request().get();
        onResponse(response).statusEquals(Response.Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '2' and entity 'E4'\"}");
    }

    @Test
    public void test_SelectById_Prefetching() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e3", "id, name, e2_id", "8, 'yyy', 1");
        insert("e3", "id, name, e2_id", "9, 'zzz', 1");

        Response response1 = target("/v1/e3/8").queryParam("include", "e2.id").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response2 = target("/v1/e3/8").queryParam("include", "e2.name").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response3 = target("/v1/e2/1").queryParam("include", "e3s.id").request().get();
        onSuccess(response3).bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void test_Select_Prefetching() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e3", "id, name, e2_id", "8, 'yyy', 1");
        insert("e3", "id, name, e2_id", "9, 'zzz', 1");

        Response response = target("/v1/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");
    }

    @Test
    public void test_Select_RelationshipSort() {

        insert("e2", "id, name", "1, 'zzz'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e2", "id, name", "3, 'xxx'");

        insert("e3", "id, name, e2_id", "8, 'aaa', 1");
        insert("e3", "id, name, e2_id", "9, 'bbb', 2");
        insert("e3", "id, name, e2_id", "10, 'ccc', 3");

        Response response = target("/v1/e3")
                .queryParam("include", "id")
                .queryParam("include", E3.E2.getName())
                .queryParam("sort", E3.E2.dot(E2.NAME).getName())
                .request()
                .get();

        onSuccess(response).bodyEquals(3,
                "{\"id\":10,\"e2\":{\"id\":3,\"address\":null,\"name\":\"xxx\"}}",
                "{\"id\":9,\"e2\":{\"id\":2,\"address\":null,\"name\":\"yyy\"}}",
                "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"zzz\"}}");
    }

    @Test
    public void test_Select_RelationshipStartLimit() throws UnsupportedEncodingException {

        insert("e2", "id, name", "1, 'zzz'");
        insert("e2", "id, name", "2, 'yyy'");

        insert("e3", "id, name, e2_id", "8, 'aaa', 1");
        insert("e3", "id, name, e2_id", "9, 'bbb', 1");
        insert("e3", "id, name, e2_id", "10, 'ccc', 2");

        Response response = target("/v1/e2")
                .queryParam("include", "id")
                .queryParam("include", URLEncoder.encode("{\"path\":\"" + E2.E3S.getName() + "\",\"start\":1,\"limit\":1}", "UTF-8"))
                .queryParam("exclude", E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().get();

        onSuccess(response).bodyEquals(2,
                "{\"id\":1,\"e3s\":[{\"id\":9,\"name\":\"bbb\"}]}",
                "{\"id\":2,\"e3s\":[]}");
    }

    @Test
    public void test_Select_Prefetching_StartLimit() {

        insert("e2", "id, name", "1, 'xxx'");

        insert("e3", "id, name, e2_id", "8, 'yyy', 1");
        insert("e3", "id, name, e2_id", "9, 'zzz', 1");
        insert("e3", "id, name, e2_id", "10, 'zzz', 1");
        insert("e3", "id, name, e2_id", "11, 'zzz', 1");

        Response response = target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .request()
                .get();

        onSuccess(response).bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}");
    }

    @Test
    public void test_SelectToOne_Null() {

        insert("e2", "id, name", "1, 'xxx'");

        insert("e3", "id, name, e2_id", "8, 'yyy', 1");
        insert("e3", "id, name, e2_id", "9, 'zzz', null");

        Response response = target("/v1/e3")
                .queryParam("include", "e2.id", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(2,
                "{\"id\":8,\"e2\":{\"id\":1}}",
                "{\"id\":9,\"e2\":null}");
    }

    @Test
    public void test_Select_MapByRootEntity() {

        insert("e4", "c_varchar, c_int", "'xxx', 1");
        insert("e4", "c_varchar, c_int", "'yyy', 2");
        insert("e4", "c_varchar, c_int", "'zzz', 2");

        Response response = target("/v1/e4")
                .queryParam("mapBy", E4.C_INT.getName())
                .queryParam("include", E4.C_VARCHAR.getName())
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"cVarchar\":\"xxx\"}]",
                "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void test_Select_MapByRootEntity_Related() {

        insert("e2", "id, name", "1, 'zzz'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "8,  1, 'aaa'");
        insert("e3", "id, e2_id, name", "9,  1, 'bbb'");
        insert("e3", "id, e2_id, name", "10, 2, 'ccc'");

        Response response = target("/v1/e3")
                .queryParam("mapBy", E3.E2.dot(E2.ID_PK_COLUMN).getName())
                .queryParam("exclude", E3.PHONE_NUMBER.getName())
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void test_SelectById_EscapeLineSeparators() {

        insert("e4", "id, c_varchar", "1, 'First line\u2028Second line...\u2029'");

        Response response = target("/v1/e4/1")
                .queryParam("include", "cVarchar")
                .request()
                .get();

        onSuccess(response).bodyEquals(1, "{\"cVarchar\":\"First line\\u2028Second line...\\u2029\"}");
    }
}
