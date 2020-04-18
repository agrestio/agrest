package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.Encoders;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.ResourceEntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncoderService_DateTime_Test {

    private AgEntity<PDate> dateEntity;
    private AgEntity<PTime> timeEntity;
    private AgEntity<PDateTime> dateTimeEntity;
    private AgEntity<POffsetDateTime> offsetDateTimeEntity;

    private EncoderService encoderService;

    @Before
    public void before() {
        this.encoderService = new EncoderService(
                new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get()),
                mock(IStringConverterFactory.class),
                new RelationshipMapper(),
                Collections.emptyMap());

        this.dateEntity = mock(AgEntity.class);
        when(dateEntity.getType()).thenReturn(PDate.class);
        when(dateEntity.getName()).thenReturn("PDate");

        this.timeEntity = mock(AgEntity.class);
        when(timeEntity.getType()).thenReturn(PTime.class);
        when(timeEntity.getName()).thenReturn("PTime");

        this.dateTimeEntity = mock(AgEntity.class);
        when(dateTimeEntity.getType()).thenReturn(PDateTime.class);
        when(dateTimeEntity.getName()).thenReturn("PDateTime");

        this.offsetDateTimeEntity = mock(AgEntity.class);
        when(offsetDateTimeEntity.getType()).thenReturn(POffsetDateTime.class);
        when(offsetDateTimeEntity.getName()).thenReturn("POffsetDateTime");
    }

    @Test
    public void testJava8ISODate() {
        ResourceEntity<PDate> re = new RootResourceEntity<>(dateEntity, null);
        ResourceEntityUtils.appendAttribute(re, "date", LocalDate.class);

        LocalDate localDate = LocalDate.now();

        PDate o = new PDate();
        o.setDate(localDate);

        String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testJava8ISOTime() {
        // fractional part is not printed, when less than a millisecond
        doTestJava8ISOTime(LocalTime.of(10, 0, 0), "HH:mm:ss");
        doTestJava8ISOTime(LocalTime.of(10, 0, 0, 1), "HH:mm:ss");
        doTestJava8ISOTime(LocalTime.of(10, 0, 0, 999_999), "HH:mm:ss");
        int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds
        doTestJava8ISOTime(LocalTime.of(10, 0, 0, millisecond), "HH:mm:ss.SSS");
    }

    private void doTestJava8ISOTime(LocalTime time, String expectedPattern) {

        ResourceEntity<PTime> re = new RootResourceEntity<>(timeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "time", LocalTime.class);

        PTime o = new PTime();
        o.setTime(time);

        String timeString = DateTimeFormatter.ofPattern(expectedPattern).format(time);
        assertEquals("{\"data\":[{\"time\":\"" + timeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testJava8ISOTimestamp() {
        // fractional part is not printed, when less than a millisecond
        doTestJava8ISOTimestamp(LocalDateTime.of(2017, 1, 1, 10, 0, 0), "yyyy-MM-dd'T'HH:mm:ss");
        doTestJava8ISOTimestamp(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), "yyyy-MM-dd'T'HH:mm:ss");
        doTestJava8ISOTimestamp(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), "yyyy-MM-dd'T'HH:mm:ss");
        int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds
        doTestJava8ISOTimestamp(LocalDateTime.of(2017, 1, 1, 10, 0, 0, millisecond), "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private void doTestJava8ISOTimestamp(LocalDateTime dateTime, String expectedPattern) {

        ResourceEntity<PDateTime> re = new RootResourceEntity<>(dateTimeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "timestamp", LocalDateTime.class);

        PDateTime o = new PDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ofPattern(expectedPattern).format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testJava8ISOOffsetDateTime() {
        // fractional part is not printed, when less than a millisecond
        doTestJava8ISOOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0), ZoneOffset.ofHours(3)));
        doTestJava8ISOOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), ZoneOffset.ofHours(3)));
        doTestJava8ISOOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), ZoneOffset.ofHours(3)));
        int millisecond = 1_000_000; // millisecond is 10^6 nanoseconds
        doTestJava8ISOOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, millisecond), ZoneOffset.ofHours(3)));
    }

    private void doTestJava8ISOOffsetDateTime(OffsetDateTime dateTime) {

        ResourceEntity<POffsetDateTime> re = new RootResourceEntity<>(offsetDateTimeEntity, null);
        ResourceEntityUtils.appendAttribute(re, "timestamp", OffsetDateTime.class);

        POffsetDateTime o = new POffsetDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }

    public class PDate {

        private LocalDate date;

        @AgAttribute
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public class PTime {

        private LocalTime time;

        @AgAttribute
        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    public class PDateTime {

        private LocalDateTime timestamp;

        @AgAttribute
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public class POffsetDateTime {

        private OffsetDateTime timestamp;

        @AgAttribute
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

}