package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.iso.SqlDateTestEntity;
import com.nhl.link.rest.it.fixture.cayenne.iso.UtilDateTestEntity;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LegacyDateEncoders_IT extends JerseyTestOnDerby {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneOffset.UTC);

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    private String localDateTimeString;
    private String utcDateTimeString;

    @Before
    public void before() {
        LocalDateTime localDateTime = LocalDateTime.of(2017, 1, 1, 10, 0, 0);
        ZonedDateTime systemDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        localDateTimeString = FORMATTER.format(systemDateTime);
        utcDateTimeString = UTC_FORMATTER.format(systemDateTime);
    }

    @Test
    public void testLegacyDateEncoders_javaSqlDate() {
        insert("utest_iso", "SQL_DATE_TEST", "ID, Date, Timestamp, Time",
                "1, '2017-01-01', '" + localDateTimeString.replace("T", " ") + "', '10:00:00'");

        Response response = target("/sqldate").request().get();
        onSuccess(response).bodyEquals("{\"data\":[" +
                "{\"id\":1," +
                "\"date\":\"2017-01-01\"," +
                "\"time\":\"10:00:00\"," +
                "\"timestamp\":\"" + utcDateTimeString + "Z\"}],\"total\":1}");
    }

    /**
     * Prior to 2.11 version, encoding of java.util.Date attributes was based on their JDBC types.
     * I.e. SQL TIME would be encoded as ISO local time (HH:mm:ss) and SQL DATE - as ISO local date (yyyy-MM-dd).
     */
    @Test
    public void testLegacyDateEncoders_javaUtilDate() {
        insert("utest_iso", "UTIL_DATE_TEST", "ID, Date, Timestamp, Time",
                "1, '2017-01-01', '" + localDateTimeString.replace("T", " ") + "', '10:00:00'");

        Response response = target("/utildate").request().get();
        onSuccess(response).bodyEquals("{\"data\":[" +
                "{\"id\":1," +
                "\"date\":\"2017-01-01\"," +
                "\"time\":\"10:00:00\"," +
                "\"timestamp\":\"" + utcDateTimeString + "Z\"}],\"total\":1}");
    }

    @Path("/")
    public static class Resource {

        @Context
        private Configuration configuration;

        @GET
        @Path("sqldate")
        public DataResponse<SqlDateTestEntity> getSqlDateTestEntities(@Context UriInfo uriInfo) {
            return LinkRest.select(SqlDateTestEntity.class, configuration).uri(uriInfo).get();
        }

        @GET
        @Path("utildate")
        public DataResponse<UtilDateTestEntity> getUtilDateTestEntities(@Context UriInfo uriInfo) {
            return LinkRest.select(UtilDateTestEntity.class, configuration).uri(uriInfo).get();
        }
    }
}
