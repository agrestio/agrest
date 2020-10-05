package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.Encoders;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.ResourceEntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderService_SqlDateTime_Test {

    // using pre-defined moments in time with and without fractional seconds
    private static final long EPOCH_MILLIS = 1458995247000L;
    private static final long EPOCH_MILLIS_WITH_FRACTION = 1458995247001L;

    private EncoderService encoderService;
    private AgEntity<PSqlDateTime> sqlDateTimeEntity;

    @BeforeEach
    public void before() {

        this.encoderService = new EncoderService(
                new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get()),
                mock(IStringConverterFactory.class),
                new RelationshipMapper(),
                Collections.emptyMap());

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        AgDataMap dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        this.sqlDateTimeEntity = dataMap.getEntity(PSqlDateTime.class);
    }

    /**
     * {@link java.sql.Date} is always formatted as ISO 8601 local date {@code yyyy-MM-dd}, e.g. 2017-01-01
     */
    @Test
    public void testJavaSqlDate() {
        ResourceEntity<PSqlDateTime> re = new RootResourceEntity<>(sqlDateTimeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "date", Date.class);
        Date date = new Date(EPOCH_MILLIS);

        PSqlDateTime o = new PSqlDateTime();
        o.setDate(date);

        String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(toLocalDateTime(date));
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    /**
     * {@link java.sql.Time} is always formatted as ISO 8601 local time {@code HH:mm:ss[.SSS]}, e.g. 10:00:00
     */
    @Test
    public void testJavaSqlTime() {
        _testISOTimeEncoder_javaSqlTime(new java.sql.Time(EPOCH_MILLIS), "HH:mm:ss");
        _testISOTimeEncoder_javaSqlTime(new java.sql.Time(EPOCH_MILLIS_WITH_FRACTION), "HH:mm:ss.SSS");
    }

    private void _testISOTimeEncoder_javaSqlTime(java.sql.Time time, String expectedPattern) {
        ResourceEntity<PSqlDateTime> re = new RootResourceEntity<>(sqlDateTimeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "time", Time.class);

        PSqlDateTime o = new PSqlDateTime();
        o.setTime(time);

        String timeString = DateTimeFormatter.ofPattern(expectedPattern).format(toLocalDateTime(time));
        assertEquals("{\"data\":[{\"time\":\"" + timeString + "\"}],\"total\":1}", toJson(o, re));
    }

    /**
     * {@link java.sql.Timestamp} is always formatted as ISO 8601 local date-time {@code yyyy-MM-dd'T'HH:mm:ss[.SSS]}, e.g. 2017-01-01T10:00:00
     */
    @Test
    public void testJavaSqlTimestamp() {
        _testISODateTimeEncoder_javaSqlTimestamp(new java.sql.Timestamp(EPOCH_MILLIS), "yyyy-MM-dd'T'HH:mm:ss");
        _testISODateTimeEncoder_javaSqlTimestamp(new java.sql.Timestamp(EPOCH_MILLIS_WITH_FRACTION), "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private void _testISODateTimeEncoder_javaSqlTimestamp(java.sql.Timestamp timestamp, String expectedPattern) {

        ResourceEntity<PSqlDateTime> re = new RootResourceEntity<>(sqlDateTimeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "timestamp", Timestamp.class);

        PSqlDateTime o = new PSqlDateTime();
        o.setTimestamp(timestamp);

        String tsString = DateTimeFormatter.ofPattern(expectedPattern).format(toLocalDateTime(timestamp));
        assertEquals("{\"data\":[{\"timestamp\":\"" + tsString + "\"}],\"total\":1}", toJson(o, re));
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }

    public class PSqlDateTime {

        private Date date;
        private Time time;
        private Timestamp timestamp;

        @AgAttribute
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @AgAttribute
        public Time getTime() {
            return time;
        }

        public void setTime(Time time) {
            this.time = time;
        }

        @AgAttribute
        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }
}
